package net.querz.mcaselector.ui;

import javafx.beans.value.ChangeListener;
import javafx.css.*;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import net.querz.mcaselector.io.ImageHelper;
import net.querz.mcaselector.tiles.Tile;
import net.querz.mcaselector.tiles.TileMap;
import net.querz.mcaselector.debug.Debug;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TileMapBox extends HBox {

	private final StyleableObjectProperty<Color> regionGridColorProperty = new SimpleStyleableObjectProperty<>(regionGridColorMetaData, this, "regionGridColor");
	private static final CssMetaData<TileMapBox, Color> regionGridColorMetaData = new CssMetaData<TileMapBox, Color>("-region-grid-color", StyleConverter.getColorConverter()) {
		@Override
		public boolean isSettable(TileMapBox styleable) {
			return !styleable.regionGridColorProperty.isBound();
		}

		@Override
		public StyleableProperty<Color> getStyleableProperty(TileMapBox styleable) {
			return styleable.regionGridColorProperty;
		}
	};

	private final StyleableObjectProperty<Color> chunkGridColorProperty = new SimpleStyleableObjectProperty<>(chunkGridColorMetaData, this, "chunkGridColor");
	private static final CssMetaData<TileMapBox, Color> chunkGridColorMetaData = new CssMetaData<TileMapBox, Color>("-chunk-grid-color", StyleConverter.getColorConverter()) {
		@Override
		public boolean isSettable(TileMapBox styleable) {
			return !styleable.chunkGridColorProperty.isBound();
		}

		@Override
		public StyleableProperty<Color> getStyleableProperty(TileMapBox styleable) {
			return styleable.chunkGridColorProperty;
		}
	};

	private final StyleableDoubleProperty gridLineWidthProperty = new SimpleStyleableDoubleProperty(gridLineWidthMetaData, this, "gridLineWidth");
	private static final CssMetaData<TileMapBox, Number> gridLineWidthMetaData = new CssMetaData<TileMapBox, Number>("-grid-line-width", StyleConverter.getSizeConverter()) {
		@Override
		public boolean isSettable(TileMapBox styleable) {
			return !styleable.gridLineWidthProperty.isBound();
		}

		@Override
		public StyleableProperty<Number> getStyleableProperty(TileMapBox styleable) {
			return styleable.gridLineWidthProperty;
		}
	};

	private final StyleableObjectProperty<Color> emptyColorProperty = new SimpleStyleableObjectProperty<>(emptyColorMetaData, this, "emptyColor");
	private static final CssMetaData<TileMapBox, Color> emptyColorMetaData = new CssMetaData<TileMapBox, Color>("-empty-color", StyleConverter.getColorConverter()) {
		@Override
		public boolean isSettable(TileMapBox styleable) {
			return !styleable.emptyColorProperty.isBound();
		}

		@Override
		public StyleableProperty<Color> getStyleableProperty(TileMapBox styleable) {
			return styleable.emptyColorProperty;
		}
	};

	private static final List<CssMetaData<? extends Styleable, ?>> CLASS_CSS_META_DATA;

	static {
		// combine already available properties in HBox with new properties
		List<CssMetaData<? extends Styleable, ?>> parent = HBox.getClassCssMetaData();
		List<CssMetaData<? extends Styleable, ?>> additional = Arrays.asList(
				regionGridColorMetaData,
				chunkGridColorMetaData,
				gridLineWidthMetaData,
				emptyColorMetaData
		);
		List<CssMetaData<? extends Styleable, ?>> own = new ArrayList<>(parent.size() + additional.size());
		own.addAll(parent);
		own.addAll(additional);
		CLASS_CSS_META_DATA = Collections.unmodifiableList(own);
	}

	public TileMapBox(TileMap tileMap, Stage primaryStage) {
		getStyleClass().add("tile-map-box");
		ChangeListener<Number> sizeListener = (o, r, n) -> {
			tileMap.resize(primaryStage.getWidth(), primaryStage.getHeight());
			Debug.dump("resizing to " + primaryStage.getWidth() + " " + primaryStage.getHeight());
		};
		primaryStage.widthProperty().addListener(sizeListener);
		primaryStage.heightProperty().addListener(sizeListener);
		setAlignment(Pos.TOP_LEFT);
		getChildren().add(tileMap);
		bind();
	}

	private void bind() {
		regionGridColorProperty.addListener((o, r, n) -> Tile.REGION_GRID_COLOR = new net.querz.mcaselector.ui.Color(regionGridColorProperty.get()));
		chunkGridColorProperty.addListener((o, r, n) -> Tile.CHUNK_GRID_COLOR = new net.querz.mcaselector.ui.Color(chunkGridColorProperty.get()));
		gridLineWidthProperty.addListener((o, r, n) -> Tile.GRID_LINE_WIDTH = gridLineWidthProperty.get());
		emptyColorProperty.addListener((o, r, n) -> {
			Tile.EMPTY_COLOR = new net.querz.mcaselector.ui.Color(emptyColorProperty.get());
			ImageHelper.reloadEmpty();
		});
	}

	@Override
	public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
		return CLASS_CSS_META_DATA;
	}
}
