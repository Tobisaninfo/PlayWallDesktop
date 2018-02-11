package de.tobias.playpad.pad;

import de.tobias.playpad.design.modern.ModernCartDesign2;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.settings.Fade;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerPoint;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.util.Duration;

import java.util.HashMap;

public class PadSettings implements Cloneable {

	// Pad Reference
	private Pad pad;

	// Settings
	private DoubleProperty volumeProperty = new SimpleDoubleProperty(1.0);
	private BooleanProperty loopProperty = new SimpleBooleanProperty(false);
	private ObjectProperty<TimeMode> timeModeProperty = new SimpleObjectProperty<>();
	private ObjectProperty<Fade> fadeProperty = new SimpleObjectProperty<>();
	private ObjectProperty<Duration> warningProperty = new SimpleObjectProperty<>();

	private BooleanProperty customDesignProperty = new SimpleBooleanProperty(false);
	private ModernCartDesign2 design;

	private HashMap<TriggerPoint, Trigger> triggers = new HashMap<>();

	private HashMap<String, Object> customSettings = new HashMap<>();

	public PadSettings(Pad pad) {
		this.pad = pad;
	}

	public double getVolume() {
		return volumeProperty.get();
	}

	public void setVolume(double volume) {
		volumeProperty.set(volume);
	}

	public DoubleProperty volumeProperty() {
		return volumeProperty;
	}

	public boolean isLoop() {
		return loopProperty.get();
	}

	public void setLoop(boolean loop) {
		this.loopProperty.set(loop);
	}

	public BooleanProperty loopProperty() {
		return loopProperty;
	}

	public boolean isCustomTimeMode() {
		return timeModeProperty.isNotNull().get();
	}

	public BooleanBinding customTimeModeProperty() {
		return timeModeProperty.isNotNull();
	}

	public TimeMode getTimeMode() {
		if (timeModeProperty.isNull().get()) {
			if (Profile.currentProfile() != null) {
				return Profile.currentProfile().getProfileSettings().getPlayerTimeDisplayMode();
			}
		}
		return timeModeProperty.get();
	}

	public void setTimeMode(TimeMode timeMode) {
		this.timeModeProperty.set(timeMode);
	}

	public ObjectProperty<TimeMode> timeModeProperty() {
		return timeModeProperty;
	}

	public boolean isCustomFade() {
		return fadeProperty.isNotNull().get();
	}

	public BooleanBinding customFadeProperty() {
		return fadeProperty.isNotNull();
	}

	/**
	 * Returns either the fade settings of this pad or the global settings
	 *
	 * @return Fade
	 */
	public Fade getFade() {
		if (fadeProperty.isNull().get()) {
			if (Profile.currentProfile() != null) {
				return Profile.currentProfile().getProfileSettings().getFade();
			}
		}
		return fadeProperty.get();
	}

	public void setFade(Fade fade) {
		this.fadeProperty.set(fade);
	}

	public ObjectProperty<Fade> fadeProperty() {
		return fadeProperty;
	}

	public boolean isCustomWarning() {
		return warningProperty.isNotNull().get();
	}

	public BooleanBinding customWarningProperty() {
		return warningProperty.isNotNull();
	}

	public Duration getWarning() {
		if (warningProperty.isNull().get()) {
			if (Profile.currentProfile() != null) {
				return Profile.currentProfile().getProfileSettings().getWarningFeedback();
			}
		}
		return warningProperty.get();
	}

	public void setWarning(Duration warning) {
		this.warningProperty.set(warning);
	}

	public ObjectProperty<Duration> warningProperty() {
		return warningProperty;
	}

	public boolean isCustomDesign() {
		return customDesignProperty.get();
	}

	public void setCustomDesign(boolean customLayout) {
		this.customDesignProperty.set(customLayout);
	}

	public BooleanProperty customDesignProperty() {
		return customDesignProperty;
	}

	public ModernCartDesign2 getDesign() {
		if (design == null) {
			design = new ModernCartDesign2(pad);
		}
		return design;
	}

	public ModernColor getBackgroundColor() {
		if (isCustomDesign()) {
			return design.getBackgroundColor();
		} else {
			return Profile.currentProfile().getProfileSettings().getDesign().getBackgroundColor();
		}
	}

	public void setDesign(ModernCartDesign2 design) {
		this.design = design;
	}

	public HashMap<String, Object> getCustomSettings() {
		return customSettings;
	}

	public HashMap<TriggerPoint, Trigger> getTriggers() {
		return triggers;
	}

	public Trigger getTrigger(TriggerPoint point) {
		return triggers.get(point);
	}

	void updateTrigger() {
		for (TriggerPoint point : TriggerPoint.values()) {
			if (!triggers.containsKey(point)) {
				Trigger trigger = new Trigger(point);
				triggers.put(point, trigger);
			}
		}
	}

	public boolean hasTriggerItems() {
		for (Trigger trigger : triggers.values()) {
			if (!trigger.getItems().isEmpty())
				return true;
		}
		return false;
	}

	public PadSettings clone(Pad pad) throws CloneNotSupportedException {
		PadSettings settings = (PadSettings) super.clone();
		settings.volumeProperty = new SimpleDoubleProperty(getVolume());
		settings.loopProperty = new SimpleBooleanProperty(isLoop());

		if (isCustomTimeMode())
			settings.timeModeProperty = new SimpleObjectProperty<>(getTimeMode());
		else
			settings.timeModeProperty = new SimpleObjectProperty<>();

		if (isCustomFade())
			settings.fadeProperty = new SimpleObjectProperty<>(getFade());
		else
			settings.fadeProperty = new SimpleObjectProperty<>();

		if (isCustomWarning())
			settings.warningProperty = new SimpleObjectProperty<>(getWarning());
		else
			settings.warningProperty = new SimpleObjectProperty<>();

		settings.customDesignProperty = new SimpleBooleanProperty(isCustomDesign());
		settings.design = design.clone(pad);

		settings.triggers = new HashMap<>(); // TODO Trigger werden nicht Kopiert
		settings.customSettings = new HashMap<>(); // TODO CustomSettings werden nicht Kopiert

		settings.updateTrigger();

		return settings;
	}
}
