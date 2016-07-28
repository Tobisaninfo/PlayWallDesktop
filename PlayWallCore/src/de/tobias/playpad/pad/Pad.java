package de.tobias.playpad.pad;

import java.nio.file.Path;
import java.util.HashMap;

import de.tobias.playpad.layout.CartLayout;
import de.tobias.playpad.layout.LayoutRegistry;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.play.Pauseable;
import de.tobias.playpad.pad.triggerlistener.PadTriggerContentListener;
import de.tobias.playpad.pad.triggerlistener.PadTriggerDurationListener;
import de.tobias.playpad.pad.triggerlistener.PadTriggerStatusListener;
import de.tobias.playpad.pad.view.IPadViewController;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Fade;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.Warning;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerPoint;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Pad {

	private IntegerProperty indexProperty = new SimpleIntegerProperty();
	private StringProperty nameProperty = new SimpleStringProperty();
	private ObjectProperty<PadStatus> statusProperty = new SimpleObjectProperty<>(PadStatus.EMPTY);

	private ObjectProperty<PadContent> contentProperty = new SimpleObjectProperty<>();

	// Settings
	private DoubleProperty volumeProperty = new SimpleDoubleProperty(1.0);
	private BooleanProperty loopProperty = new SimpleBooleanProperty(false);
	private ObjectProperty<TimeMode> timeModeProperty = new SimpleObjectProperty<>();
	private ObjectProperty<Fade> fadeProperty = new SimpleObjectProperty<>();
	private ObjectProperty<Warning> warningProperty = new SimpleObjectProperty<>();

	private BooleanProperty customLayoutProperty = new SimpleBooleanProperty(false);
	private HashMap<String, CartLayout> layouts = new HashMap<>();

	private HashMap<String, Object> customSettings = new HashMap<>();

	// Trigger
	private HashMap<TriggerPoint, Trigger> triggers = new HashMap<>();

	// Custom Volume
	private transient DoubleProperty customVolumeProperty = new SimpleDoubleProperty(1.0);

	// Global Listener (unabhängig von der UI), für Core Functions wie Play, Pause
	private transient PadStatusListener padStatusListener;

	// Trigger Listener
	private transient PadTriggerStatusListener padTriggerStatusListener;
	private transient PadTriggerDurationListener padTriggerDurationListener;
	private transient PadTriggerContentListener padTriggerContentListener;
	private transient boolean ignoreTrigger = false;

	// Utils
	private transient boolean eof;
	private transient IPadViewController controller;
	private transient Project project;

	public Pad(Project project) {
		this.project = project;

		initPadListener();
		// Update Trigger ist nicht notwendig, da es in load(Element) ausgerufen wird
	}

	public Pad(Project project, int index) {
		this.project = project;
		setIndex(index);
		setStatus(PadStatus.EMPTY);

		initPadListener();
		updateTrigger();
	}

	public Pad(Project project, int index, String name, PadContent content) {
		this(project, index);
		setName(name);
		setContent(content);
	}

	private void initPadListener() {
		padStatusListener = new PadStatusListener(this);
		statusProperty.addListener(padStatusListener);

		padTriggerStatusListener = new PadTriggerStatusListener(this);
		statusProperty.addListener(padTriggerStatusListener);

		padTriggerDurationListener = new PadTriggerDurationListener(this);

		// Das ist für die Position Listener notwendig, wenn sich der Content ändert
		padTriggerContentListener = new PadTriggerContentListener(this);
		padTriggerContentListener.changed(contentProperty, null, getContent());
	}

	// Accessor Methods
	public int getIndex() {
		return indexProperty.get();
	}

	public int getIndexReadable() {
		return indexProperty.get() + 1;
	}

	public void setIndex(int index) {
		this.indexProperty.set(index);
	}

	public ReadOnlyIntegerProperty indexProperty() {
		return indexProperty;
	}

	public String getName() {
		return nameProperty.get();
	}

	public void setName(String name) {
		this.nameProperty.set(name);
	}

	public StringProperty nameProperty() {
		return nameProperty;
	}

	public PadStatus getStatus() {
		return statusProperty.get();
	}

	public void setStatus(PadStatus status) {
		// PLay, Pause & Stop nut wenn Pad Content hat
		if (status == PadStatus.PLAY || status == PadStatus.STOP || status == PadStatus.PAUSE) {
			if (this.statusProperty.get() == PadStatus.EMPTY) {
				return;
			}
		}
		// Pause nur wenn Pause möglich
		if (status == PadStatus.PAUSE && !(getContent() instanceof Pauseable)) {
			return;
		}
		// Stop nicht wenn Ready (Stop/Keine Wiedergabe)
		if (status == PadStatus.STOP && getStatus() == PadStatus.READY) {
			return;
		}
		this.statusProperty.set(status);
	}

	public void setStatus(PadStatus status, boolean ignoreTrigger) {
		this.ignoreTrigger = ignoreTrigger;
		setStatus(status);
	}

	public ObjectProperty<PadStatus> statusProperty() {
		return statusProperty;
	}

	public PadContent getContent() {
		return contentProperty.get();
	}

	public void setContent(PadContent content) {
		this.contentProperty.set(content);
	}

	public ObjectProperty<PadContent> contentProperty() {
		return contentProperty;
	}

	public double getVolume() {
		return volumeProperty.get();
	}

	public void setVolume(double volume) {
		volumeProperty.set(volume);
	}

	public void setMasterVolume(double volume) {
		if (getContent() != null) {
			getContent().setMasterVolume(volume);
		}
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
	 * @return
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

	public Warning getWarning() {
		if (warningProperty.isNull().get()) {
			if (Profile.currentProfile() != null) {
				return Profile.currentProfile().getProfileSettings().getWarningFeedback();
			}
		}
		return warningProperty.get();
	}

	public void setWarning(Warning warning) {
		this.warningProperty.set(warning);
	}

	public ObjectProperty<Warning> warningProperty() {
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

	public CartLayout getLayout() {
		return getLayout(Profile.currentProfile().getProfileSettings().getLayoutType());
	}

	public CartLayout getLayout(String type) {
		if (!layouts.containsKey(type)) {
			layouts.put(type, LayoutRegistry.getLayout(type).newCartLayout());
		}
		return layouts.get(type);
	}

	public void setLayout(CartLayout layout, String type) {
		this.layouts.put(type, layout);
	}

	public boolean isEof() {
		return eof;
	}

	public void setEof(boolean eof) {
		this.eof = eof;
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

	// Helper Methodes
	public void loadContent() throws NoSuchComponentException {
		if (contentProperty.get() != null)
			contentProperty.get().loadMedia();
	}

	public void throwException(Path path, Exception exception) {
		if (project != null)
			project.addException(this, path, exception);
		setStatus(PadStatus.ERROR);
	}

	public void removeExceptionsForPad() {
		if (project != null)
			project.removeExceptions(this);
	}

	public void removeException(PadException exception) {
		if (project != null)
			project.removeException(exception);
	}

	public PadTriggerDurationListener getPadTriggerDurationListener() {
		return padTriggerDurationListener;
	}

	public boolean isIgnoreTrigger() {
		return ignoreTrigger;
	}

	public void setIgnoreTrigger(boolean ignoreTrigger) {
		this.ignoreTrigger = ignoreTrigger;
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

	public Project getProject() {
		return project;
	}

	public boolean isPadVisible() {
		return controller != null;
	}

	public IPadViewController getController() {
		return controller;
	}

	public void setController(IPadViewController controller) {
		this.controller = controller;
	}

	public void clear() {
		setName("");
		if (contentProperty.isNotNull().get())
			contentProperty.get().unloadMedia();
		setContent(null);
		setStatus(PadStatus.EMPTY);

		if (project != null) {
			project.removeExceptions(this);
		}
	}

	@Override
	public String toString() {
		return "Pad: " + indexProperty.get() + " - " + nameProperty.get();
	}

	public String toReadableString() {
		return (indexProperty.get() + 1) + " - " + nameProperty.get();
	}

	// TODO Reorder
	public void setCustomVolume(double volume) {
		customVolumeProperty.set(volume);
	}

	public double getCustomVolume() {
		return customVolumeProperty.get();
	}

	public DoubleProperty customVolumeProperty() {
		return customVolumeProperty;
	}

	HashMap<String, CartLayout> getLayouts() {
		return layouts;
	}
}
