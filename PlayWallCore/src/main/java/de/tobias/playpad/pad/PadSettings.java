package de.tobias.playpad.pad;

import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.design.modern.model.ModernCartDesign;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.listener.upstream.PadSettingsUpdateListener;
import de.tobias.playpad.settings.FadeSettings;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerPoint;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.util.Duration;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PadSettings {

	// Pad Reference
	private Pad pad;

	private UUID id;

	// Settings
	private DoubleProperty volumeProperty = new SimpleDoubleProperty(1.0);
	private DoubleProperty speedProperty = new SimpleDoubleProperty(1.0);
	private BooleanProperty loopProperty = new SimpleBooleanProperty(false);
	private ObjectProperty<TimeMode> timeModeProperty = new SimpleObjectProperty<>();
	private ObjectProperty<FadeSettings> fadeProperty = new SimpleObjectProperty<>();
	private ObjectProperty<Duration> warningProperty = new SimpleObjectProperty<>();
	private ObjectProperty<Duration> cueInProperty = new SimpleObjectProperty<>();

	private BooleanProperty customDesignProperty = new SimpleBooleanProperty(false);
	private ModernCartDesign design;

	private Map<TriggerPoint, Trigger> triggers = new EnumMap<>(TriggerPoint.class);
	private Map<String, Object> customSettings = new HashMap<>();

	// Sync Listener
	private PadSettingsUpdateListener syncListener;

	public PadSettings(Pad pad) {
		this.pad = pad;
		this.syncListener = new PadSettingsUpdateListener(this);
		this.id = UUID.randomUUID();
	}

	public PadSettings(Pad pad, UUID uuid) {
		this.pad = pad;
		this.syncListener = new PadSettingsUpdateListener(this);
		this.id = uuid;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
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

	public double getSpeed() {
		return speedProperty.get();
	}

	public void setSpeed(double rate) {
		this.speedProperty.set(rate);
	}

	public DoubleProperty speedProperty() {
		return speedProperty;
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
		if (timeModeProperty.isNull().get() && Profile.currentProfile() != null) {
			return Profile.currentProfile().getProfileSettings().getPlayerTimeDisplayMode();
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
	public FadeSettings getFade() {
		if (fadeProperty.isNull().get() && Profile.currentProfile() != null) {
			return Profile.currentProfile().getProfileSettings().getFade();
		}
		return fadeProperty.get();
	}

	public void setFade(FadeSettings fade) {
		this.fadeProperty.set(fade);
	}

	public ObjectProperty<FadeSettings> fadeProperty() {
		return fadeProperty;
	}

	public boolean isCustomWarning() {
		return warningProperty.isNotNull().get();
	}

	public BooleanBinding customWarningProperty() {
		return warningProperty.isNotNull();
	}

	public Duration getWarning() {
		if (warningProperty.isNull().get() && Profile.currentProfile() != null) {
			return Profile.currentProfile().getProfileSettings().getWarningFeedback();
		}
		return warningProperty.get();
	}

	public void setWarning(Duration warning) {
		this.warningProperty.set(warning);
	}

	public ObjectProperty<Duration> warningProperty() {
		return warningProperty;
	}

	public Duration getCueIn() {
		return cueInProperty.get();
	}

	public void setCueIn(Duration cueIn) {
		cueInProperty.set(cueIn);
	}

	public Duration cueInProperty() {
		return cueInProperty.get();
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

	public ModernCartDesign getDesign() {
		if (design == null) {
			ModernCartDesign newDesign = new ModernCartDesign.ModernCartDesignBuilder(pad).build();

			if (pad.getProject().getProjectReference().isSync()) {
				CommandManager.execute(Commands.DESIGN_ADD, pad.getProject().getProjectReference(), newDesign);
			}

			setDesign(newDesign);

		}
		return design;
	}

	public void setDesign(ModernCartDesign design) {
		this.design = design;
		if (pad.getProject().getProjectReference().isSync()) {
			design.addListener();
		}
	}

	public Map<String, Object> getCustomSettings() {
		return customSettings;
	}

	public Map<TriggerPoint, Trigger> getTriggers() {
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

	public PadSettings copy(Pad pad) {
		PadSettings clone = new PadSettings(pad);
		clone.id = UUID.randomUUID();

		clone.volumeProperty = new SimpleDoubleProperty(getVolume());
		clone.loopProperty = new SimpleBooleanProperty(isLoop());

		if (isCustomTimeMode())
			clone.timeModeProperty = new SimpleObjectProperty<>(getTimeMode());
		else
			clone.timeModeProperty = new SimpleObjectProperty<>();

		if (isCustomFade())
			clone.fadeProperty = new SimpleObjectProperty<>(getFade());
		else
			clone.fadeProperty = new SimpleObjectProperty<>();

		if (isCustomWarning())
			clone.warningProperty = new SimpleObjectProperty<>(getWarning());
		else
			clone.warningProperty = new SimpleObjectProperty<>();

		clone.customDesignProperty = new SimpleBooleanProperty(isCustomDesign());
		if (design != null) {
			clone.design = design.copy(pad);
		}

		clone.triggers = new EnumMap<>(TriggerPoint.class);
		triggers.forEach((key, value) -> clone.triggers.put(key, value.copy()));

		clone.customSettings = new HashMap<>(); // TODO CustomSettings werden nicht Kopiert

		clone.updateTrigger();

		final ProjectReference projectReference = pad.getProject().getProjectReference();
		if (projectReference.isSync()) {
			CommandManager.execute(Commands.PAD_SETTINGS_ADD, projectReference, clone);
		}

		return clone;
	}

	public Pad getPad() {
		return pad;
	}


	public void addSyncListener() {
		syncListener.addListener();
	}

	public void removeListener() {
		syncListener.removeListener();
	}

	//// Computed

	public ModernColor getBackgroundColor() {
		if (isCustomDesign()) {
			return design.getBackgroundColor();
		} else {
			return Profile.currentProfile().getProfileSettings().getDesign().getBackgroundColor();
		}
	}
}
