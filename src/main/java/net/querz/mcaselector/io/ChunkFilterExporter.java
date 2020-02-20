package net.querz.mcaselector.io;

import net.querz.mcaselector.Config;
import net.querz.mcaselector.filter.GroupFilter;
import net.querz.mcaselector.debug.Debug;
import net.querz.mcaselector.point.Point2i;
import net.querz.mcaselector.progress.Progress;
import net.querz.mcaselector.progress.Timer;
import net.querz.mcaselector.text.Translation;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

public class ChunkFilterExporter {

	private ChunkFilterExporter() {}

	public static void exportFilter(GroupFilter filter, Map<Point2i, Set<Point2i>> selection, File destination, Progress progressChannel, boolean headless) {
		File[] files = Config.getWorldDir().listFiles((d, n) -> n.matches(FileHelper.MCA_FILE_PATTERN));
		if (files == null || files.length == 0) {
			if (headless) {
				progressChannel.done("no files");
			} else {
				progressChannel.done(Translation.DIALOG_PROGRESS_NO_FILES.toString());
			}
			return;
		}

		MCAFilePipe.clearQueues();

		progressChannel.setMax(files.length);
		progressChannel.updateProgress(files[0].getName(), 0);

		for (File file : files) {
			MCAFilePipe.addJob(new MCAExportFilterLoadJob(file, filter, selection, destination, progressChannel));
		}
	}

	private static class MCAExportFilterLoadJob extends LoadDataJob {

		private GroupFilter filter;
		private Map<Point2i, Set<Point2i>> selection;
		private Progress progressChannel;
		private File destination;

		private MCAExportFilterLoadJob(File file, GroupFilter filter, Map<Point2i, Set<Point2i>> selection, File destination, Progress progressChannel) {
			super(file);
			this.filter = filter;
			this.selection = selection;
			this.destination = destination;
			this.progressChannel = progressChannel;
		}

		@Override
		public void execute() {
			Matcher m = FileHelper.REGION_GROUP_PATTERN.matcher(getFile().getName());
			if (m.find()) {
				int regionX = Integer.parseInt(m.group("regionX"));
				int regionZ = Integer.parseInt(m.group("regionZ"));
				Point2i location = new Point2i(regionX, regionZ);

				if (!filter.appliesToRegion(location) || selection != null && !selection.containsKey(location)) {
					Debug.dump("filter does not apply to file " + getFile().getName());
					progressChannel.incrementProgress(getFile().getName());
					return;
				}

				//copy file to new directory
				File to = new File(destination, getFile().getName());
				if (to.exists()) {
					Debug.dumpf("%s exists, not overwriting", to.getAbsolutePath());
					progressChannel.incrementProgress(getFile().getName());
					return;
				}

				byte[] data = load();
				if (data != null) {
					MCAFilePipe.executeProcessData(new MCAExportFilterProcessJob(getFile(), data, filter, selection == null ? null : selection.get(location), to, progressChannel));
				} else {
					Debug.errorf("error loading mca file %s", getFile().getName());
					progressChannel.incrementProgress(getFile().getName());
				}
			} else {
				Debug.dump("wtf, how did we get here??");
				progressChannel.incrementProgress(getFile().getName());
			}
		}
	}

	private static class MCAExportFilterProcessJob extends ProcessDataJob {

		private Progress progressChannel;
		private GroupFilter filter;
		private Set<Point2i> selection;
		private File destination;

		private MCAExportFilterProcessJob(File file, byte[] data, GroupFilter filter, Set<Point2i> selection, File destination, Progress progressChannel) {
			super(file, data);
			this.filter = filter;
			this.selection = selection;
			this.destination = destination;
			this.progressChannel = progressChannel;
		}

		@Override
		public void execute() {
			//load MCAFile
			Timer t = new Timer();
			try {
				MCAFile mca = MCAFile.readAll(getFile(), new ByteArrayPointer(getData()));
				if (mca != null) {
					mca.keepChunkIndices(filter, selection);
					Debug.dumpf("took %s to delete chunk indices in %s", t, getFile().getName());
					MCAFilePipe.executeSaveData(new MCAExportFilterSaveJob(getFile(), mca, destination, progressChannel));
				}
			} catch (Exception ex) {
				progressChannel.incrementProgress(getFile().getName());
				Debug.errorf("error deleting chunk indices in %s", getFile().getName());
			}
		}
	}

	private static class MCAExportFilterSaveJob extends SaveDataJob<MCAFile> {

		private File destination;
		private Progress progressChannel;

		private MCAExportFilterSaveJob(File file, MCAFile data, File destination, Progress progressChannel) {
			super(file, data);
			this.destination = destination;
			this.progressChannel = progressChannel;
		}

		@Override
		public void execute() {
			Timer t = new Timer();
			try {
				File tmpFile;
				try (RandomAccessFile raf = new RandomAccessFile(getFile(), "r")) {
					tmpFile = getData().deFragment(raf);
				}

				if (tmpFile != null) {
					Files.move(tmpFile.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (Exception ex) {
				Debug.error(ex);
			}
			progressChannel.incrementProgress(getFile().getName());
			Debug.dumpf("took %s to save data to %s", t, getFile().getName());
		}
	}
}
