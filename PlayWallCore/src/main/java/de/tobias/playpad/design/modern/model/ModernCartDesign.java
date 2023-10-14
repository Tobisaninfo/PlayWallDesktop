package de.tobias.playpad.design.modern.model;

import de.tobias.playpad.design.FeedbackDesignColorSuggester;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.listener.upstream.DesignUpdateListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

import java.util.UUID;

public class ModernCartDesign implements FeedbackDesignColorSuggester {

	public static class ModernCartDesignBuilder {
		private final Pad pad;
		private final UUID id;

		private ModernColor backgroundColor = DEFAULT_COLOR_BACKGROUND;
		private Boolean enableCustomBackgroundColor = false;

		private ModernColor playColor = DEFAULT_COLOR_PLAY;
		private Boolean enableCustomPlayColor = false;

		private ModernColor cueInColor = DEFAULT_COLOR_CUE_IN;
		private Boolean enableCustomCueInColor = false;

		public ModernCartDesignBuilder(Pad pad) {
			this.pad = pad;
			this.id = UUID.randomUUID();
		}

		public ModernCartDesignBuilder(Pad pad, UUID id) {
			this.pad = pad;
			this.id = id;
		}

		public ModernCartDesignBuilder withBackgroundColor(ModernColor backgroundColor, Boolean enable) {
			this.backgroundColor = backgroundColor;
			this.enableCustomBackgroundColor = enable;
			return this;
		}

		public ModernCartDesignBuilder withPlayColor(ModernColor playColor, Boolean enable) {
			this.playColor = playColor;
			this.enableCustomPlayColor = enable;
			return this;
		}

		public ModernCartDesignBuilder withCueInColor(ModernColor cueInColor, Boolean enable) {
			this.cueInColor = cueInColor;
			this.enableCustomCueInColor = enable;
			return this;
		}

		public ModernCartDesign build() {
			return new ModernCartDesign(pad, id,
					backgroundColor, enableCustomBackgroundColor,
					playColor, enableCustomPlayColor,
					cueInColor, enableCustomCueInColor);
		}
	}

	public static final ModernColor DEFAULT_COLOR_BACKGROUND = ModernColor.GRAY1;
	public static final ModernColor DEFAULT_COLOR_PLAY = ModernColor.RED3;
	public static final ModernColor DEFAULT_COLOR_CUE_IN = ModernColor.RED2;

	private final Pad pad;
	private final UUID uuid;

	private BooleanProperty enableCustomBackgroundColor;
	private ObjectProperty<ModernColor> backgroundColor;

	private BooleanProperty enableCustomPlayColor;
	private ObjectProperty<ModernColor> playColor;

	private BooleanProperty enableCustomCueInColor;
	private ObjectProperty<ModernColor> cueInColor;

	private DesignUpdateListener syncListener;

	private ModernCartDesign(Pad pad, UUID id,
							 ModernColor backgroundColor, Boolean enableCustomBackgroundColor,
							 ModernColor playColor, Boolean enableCustomPlayColor,
							 ModernColor cueInColor, Boolean enableCustomCueInColor) {
		this.uuid = id;
		this.pad = pad;

		this.enableCustomBackgroundColor = new SimpleBooleanProperty(enableCustomBackgroundColor);
		this.backgroundColor = new SimpleObjectProperty<>(backgroundColor);

		this.enableCustomPlayColor = new SimpleBooleanProperty(enableCustomPlayColor);
		this.playColor = new SimpleObjectProperty<>(playColor);

		this.enableCustomCueInColor = new SimpleBooleanProperty(enableCustomCueInColor);
		this.cueInColor = new SimpleObjectProperty<>(cueInColor);

		syncListener = new DesignUpdateListener(this);
	}

	public UUID getId() {
		return uuid;
	}

	public Pad getPad() {
		return pad;
	}

	public boolean isEnableCustomBackgroundColor() {
		return enableCustomBackgroundColor.get();
	}

	public void setEnableCustomBackgroundColor(Boolean enableCustomBackgroundColor) {
		this.enableCustomBackgroundColor.set(enableCustomBackgroundColor);
	}

	public BooleanProperty enableCustomBackgroundColorProperty() {
		return enableCustomBackgroundColor;
	}

	public ModernColor getBackgroundColor() {
		return backgroundColor.get();
	}

	public void setBackgroundColor(ModernColor backgroundColor) {
		this.backgroundColor.set(backgroundColor);
	}

	public ObjectProperty<ModernColor> backgroundColorProperty() {
		return backgroundColor;
	}

	public boolean isEnableCustomPlayColor() {
		return enableCustomPlayColor.get();
	}

	public void setEnableCustomPlayColor(boolean enableCustomPlayColor) {
		this.enableCustomPlayColor.set(enableCustomPlayColor);
	}

	public BooleanProperty enableCustomPlayColorProperty() {
		return enableCustomPlayColor;
	}

	public ModernColor getPlayColor() {
		return playColor.get();
	}

	public void setPlayColor(ModernColor playColor) {
		this.playColor.set(playColor);
	}

	public ObjectProperty<ModernColor> playColorProperty() {
		return playColor;
	}

	public boolean isEnableCustomCueInColor() {
		return enableCustomCueInColor.get();
	}

	public void setEnableCustomCueInColor(boolean enableCustomCueInColor) {
		this.enableCustomCueInColor.set(enableCustomCueInColor);
	}

	public BooleanProperty enableCustomCueInColorProperty() {
		return enableCustomCueInColor;
	}

	public ModernColor getCueInColor() {
		return cueInColor.get();
	}

	public void setCueInColor(ModernColor cueInColor) {
		this.cueInColor.set(cueInColor);
	}

	public ObjectProperty<ModernColor> cueInColorProperty() {
		return cueInColor;
	}

	public void addListener() {
		syncListener.addListener();
	}

	public void removeListener() {
		syncListener.removeListener();
	}

	public void reset() {
		backgroundColor.set(DEFAULT_COLOR_BACKGROUND);
		playColor.set(DEFAULT_COLOR_PLAY);
		cueInColor.set(DEFAULT_COLOR_CUE_IN);
	}

	// Color Associate
	@Override
	public Color getDesignEventColor() {
		return Color.web(playColor.get().getColorHi());
	}

	@Override
	public Color getDesignDefaultColor() {
		return Color.web(backgroundColor.get().getColorHi());
	}

	public ModernCartDesign copy(Pad pad) {
		ModernCartDesign clone = new ModernCartDesignBuilder(pad).build();

		clone.enableCustomBackgroundColor = new SimpleBooleanProperty(isEnableCustomBackgroundColor());
		clone.backgroundColor = new SimpleObjectProperty<>(getBackgroundColor());

		clone.enableCustomPlayColor = new SimpleBooleanProperty(isEnableCustomPlayColor());
		clone.playColor = new SimpleObjectProperty<>(getPlayColor());

		clone.enableCustomCueInColor = new SimpleBooleanProperty(isEnableCustomCueInColor());
		clone.cueInColor = new SimpleObjectProperty<>(getCueInColor());

		syncListener = new DesignUpdateListener(clone);
		if (pad.getProject().getProjectReference().isSync()) {
			CommandManager.execute(Commands.DESIGN_ADD, pad.getProject().getProjectReference(), clone);
			clone.addListener();
		}

		return clone;
	}
}
