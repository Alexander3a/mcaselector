package net.querz.mcaselector.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import net.querz.mcaselector.progress.Progress;
import net.querz.mcaselector.text.Translation;

public abstract class ProgressTask extends Task<Void> implements Progress {

	private int max;
	private int current = 0;
	private final StringProperty infoProperty = new SimpleStringProperty(Translation.DIALOG_PROGRESS_RUNNING.toString());
	private Runnable onFinish;
	private boolean locked = false;

	public ProgressTask() {}

	public ProgressTask(int max) {
		this.max = max;
	}

	@Override
	public void setMax(int max) {
		this.max = max;
	}

	@Override
	public void incrementProgress(String info) {
		updateProgress(info, ++current, max);
	}

	@Override
	public void incrementProgress(String info, int count) {
		updateProgress(info, current += count, max);
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	@Override
	public void done(String info) {
		updateProgress(info, 1, 1);
	}

	@Override
	public void setMessage(String msg) {
		Platform.runLater(() -> infoProperty.setValue(msg));
	}

	public void setIndeterminate(String info) {
		Platform.runLater(() -> infoProperty.setValue(info));
		updateProgress(-1, 0);
	}

	@Override
	public void updateProgress(String info, int progress) {
		updateProgress(info, progress, max);
	}

	public void updateProgress(String info, double progress, int max) {
		if (!locked) {
			current = (int) progress;
			Platform.runLater(() -> infoProperty.setValue(info));
			updateProgress(progress, max);
			if (progress == max && onFinish != null) {
				System.out.println("GOING TO RUN ONFINISH");
				Platform.runLater(onFinish);
			}
		}
	}

	public StringProperty infoProperty() {
		return infoProperty;
	}

	public void setOnFinish(Runnable r) {
		onFinish = r;
	}
}
