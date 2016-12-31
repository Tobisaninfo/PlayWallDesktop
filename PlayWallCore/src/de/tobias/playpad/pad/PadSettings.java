package de.tobias.playpad.pad;

import java.util.HashMap;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.DesignFactory;
import de.tobias.playpad.registry.DefaultRegistry;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Fade;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerPoint;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;

public class PadSettings implements Cloneable {

	// Settings
	private DoubleProperty volumeProperty = new SimpleDoubleProperty(1.0);
	private BooleanProperty loopProperty = new SimpleBooleanProperty(false);
	private ObjectProperty<TimeMode> timeModeProperty = new SimpleObjectProperty<>();
	private ObjectProperty<Fade> fadeProperty = new SimpleObjectProperty<>();
	private ObjectProperty<Duration> warningProperty = new SimpleObjectProperty<>();

	private BooleanProperty customLayoutProperty = new SimpleBooleanProperty(false);
	private HashMap<String, CartDesign> layouts = new HashMap<>();

	private HashMap<TriggerPoint, Trigger> triggers = new HashMap<>();

	private HashMap<String, Object> customSettings = new HashMap<>();

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

	public boolean isCustomLayout() {
		return customLayoutProperty.get();
	}

	public void setCustomLayout(boolean customLayout) {
		this.customLayoutProperty.set(customLayout);
	}

	public BooleanProperty customLayoutProperty() {
		return customLayoutProperty;
	}

	public CartDesign getDesign() {
		return getLayout(Profile.currentProfile().getProfileSettings().getLayoutType());
	}

	HashMap<String, CartDesign> getLayouts() {
		return layouts;
	}

	public CartDesign getLayout(String type) {
		if (!layouts.containsKey(type)) {
			DefaultRegistry<DesignFactory> layouts2 = PlayPadPlugin.getRegistryCollection().getDesigns();
			try {
				layouts.put(type, layouts2.getFactory(type).newCartDesign());
			} catch (NoSuchComponentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return layouts.get(type);
	}

	public void setLayout(CartDesign layout, String type) {
		this.layouts.put(type, layout);
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

	@Override
	public PadSettings clone() throws CloneNotSupportedException {
		PadSettings settings = (PadSettings) super.clone();
		settings.volumeProperty = new SimpleDoubleProperty(getVolume());
		settings.loopProperty = new SimpleBooleanProperty(isLoop());

		if (isCustomTimeMode())
			settings.timeModeProperty = new SimpleObjectProperty<TimeMode>(getTimeMode());
		else
			settings.timeModeProperty = new SimpleObjectProperty<TimeMode>();

		if (isCustomFade())
			settings.fadeProperty = new SimpleObjectProperty<>(getFade());
		else
			settings.fadeProperty = new SimpleObjectProperty<>();

		if (isCustomWarning())
			settings.warningProperty = new SimpleObjectProperty<>(getWarning());
		else
			settings.warningProperty = new SimpleObjectProperty<>();

		settings.customLayoutProperty = new SimpleBooleanProperty(isCustomLayout());
		settings.layouts = new HashMap<>();
		for (String key : layouts.keySet()) {
			CartDesign clone = (CartDesign) layouts.get(key).clone();
			settings.layouts.put(key, clone);
		}

		settings.triggers = new HashMap<>(); // TODO Trigger werden nicht Kopiert
		settings.customSettings = new HashMap<>(); // TODO CustomSettings werden nicht Kopiert

		settings.updateTrigger();

		return settings;
	}
}
