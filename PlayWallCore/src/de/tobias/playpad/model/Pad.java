package de.tobias.playpad.model;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Optional;

import de.tobias.playpad.audio.AudioHandler;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.model.layout.CartLayout;
import de.tobias.playpad.model.layout.LayoutRegistry;
import de.tobias.playpad.model.settings.Warning;
import de.tobias.playpad.pad.Fade;
import de.tobias.playpad.plugin.PadListener;
import de.tobias.playpad.plugin.PlayPadPlugin;
import de.tobias.playpad.plugin.viewcontroller.IPadViewController;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import javafx.animation.Transition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Duration;

public class Pad implements Comparable<Pad> {

	public enum State {
		PLAY,
		STOP,
		EMPTY,
		READY,
		PAUSE;
	}

	public enum TimeMode {
		PLAYED,
		REST,
		BOTH;
	}

	private IntegerProperty indexProperty;
	private ObjectProperty<State> stateProperty; // Änderungen in PadView -> Wird hier dann Methoden aufgerufen

	private ObjectProperty<AudioHandler> audioHandlerProperty;

	private StringProperty pathProperty; // URI Format
	private StringProperty titleProperty;

	private BooleanProperty loopProperty;
	private double volumeProperty = 1;

	// Layout
	private boolean customLayout = false;
	private HashMap<String, CartLayout> layouts = new HashMap<>();

	// Settings
	private Optional<TimeMode> timeMode = Optional.empty();
	private Optional<Warning> warningFeedback = Optional.empty();
	private Optional<Fade> fade = Optional.empty();

	private HashMap<String, Object> userInfo = new HashMap<>();

	// Temporäre Variablen
	private transient boolean isPlaying = false;
	private transient boolean isEof = false;

	private transient Transition transition;

	// GUI und Project Referenz
	private transient IPadViewController controller;
	private transient ObjectProperty<PadException> lastExceptionProperty;
	private transient Project project;

	public Pad(int index, Project project) {
		this(index, null, null, State.EMPTY, project);
	}

	public Pad(int index, String path, String title, State state, Project project) {
		this(index, path, title, state, true, project);
	}

	public Pad(int index, String path, String title, State state, boolean loadMedia, Project project) {
		this.indexProperty = new SimpleIntegerProperty(index);
		this.pathProperty = new SimpleStringProperty(path);
		this.titleProperty = new SimpleStringProperty(title);
		this.stateProperty = new SimpleObjectProperty<Pad.State>(state);

		this.lastExceptionProperty = new SimpleObjectProperty<>();
		this.loopProperty = new SimpleBooleanProperty();

		this.project = project;

		this.audioHandlerProperty = new SimpleObjectProperty<>(AudioRegistry.geAudioType().createAudioHandler());

		if (path != null && loadMedia)
			loadMedia();
	}

	/*
	 * Getter
	 */
	public int getIndex() {
		return indexProperty.get();
	}

	public State getState() {
		return stateProperty.get();
	}

	public AudioHandler getAudioHandler() {
		return audioHandlerProperty.get();
	}

	/**
	 * File in URI Form
	 * 
	 * @return URI for Resource
	 */
	public String getFile() {
		return pathProperty.get();
	}

	public Path getPath() throws MalformedURLException, URISyntaxException {
		if (getFile() != null)
			return Paths.get(new URL(getFile()).toURI());
		return null;
	}

	public String getFileName() throws MalformedURLException, URISyntaxException {
		return getPath().getFileName().toString();
	}

	public String getTitle() {
		return titleProperty.get();
	}

	public boolean isLoop() {
		return loopProperty.get();
	}

	public double getVolume() {
		return volumeProperty;
	}

	public boolean isCustomLayout() {
		return customLayout;
	}

	public Optional<CartLayout> getLayout(String type) {
		if (!layouts.containsKey(type)) {
			try {
				CartLayout layout = LayoutRegistry.newCartLayoutInstance(type);
				layouts.put(type, layout);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
				return Optional.empty();
			}
		}
		return Optional.of(layouts.get(type));
	}

	public Optional<CartLayout> currentLayout() {
		return getLayout(Profile.currentProfile().getProfileSettings().getLayoutType());
	}

	public HashMap<String, CartLayout> getLayouts() {
		return layouts;
	}

	public boolean isCustomTimeMode() {
		return timeMode.isPresent();
	}

	public Optional<TimeMode> getTimeMode() {
		return timeMode;
	}

	public boolean isCustomWarning() {
		return warningFeedback.isPresent();
	}

	public Optional<Warning> getWarningFeedback() {
		return warningFeedback;
	}

	public boolean isCustomFade() {
		return fade.isPresent();
	}

	public Optional<Fade> getFade() {
		return fade;
	}

	public HashMap<String, Object> getUserInfo() {
		return userInfo;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	/*
	 * Setter
	 */

	void setIndex(int index) {
		this.indexProperty.set(index);
	}

	public void setState(State state) {
		if (this.stateProperty.get() != State.EMPTY || state == State.READY) {
			this.stateProperty.set(state);
		}
	}

	public void setPath(Path path) {
		setPath(path, true);
	}

	public void setPath(Path path, boolean load) {
		throwException(null);

		String uri = path.toUri().toString();
		String name = path.getFileName().toString();

		this.pathProperty.set(uri);
		this.titleProperty.set(name.substring(0, name.lastIndexOf(".")));
		if (load)
			loadMedia();
	}

	public void setTitle(String title) {
		this.titleProperty.set(title);
	}

	public void setLoop(boolean loop) {
		this.loopProperty.set(loop);
	}

	public void setVolume(double volume) {
		if (volume <= 1.0 && volume >= 0.0) {
			this.volumeProperty = volume;
			audioHandlerProperty.get().setVolume(this, volume, getMasterVolume());
		}
	}

	public void setLayout(String type, CartLayout layout) {
		layouts.put(type, layout);
	}

	public void setCustomLayout(boolean b) {
		customLayout = b;
	}

	public void setTimeMode(TimeMode timeMode) {
		if (timeMode != null) {
			this.timeMode = Optional.of(timeMode);
		} else {
			this.timeMode = Optional.empty();
		}
	}

	public void setWarningFeedback(Warning warningFeedback) {
		if (warningFeedback != null) {
			this.warningFeedback = Optional.of(warningFeedback);
		} else {
			this.warningFeedback = Optional.empty();
		}
	}

	public void setFade(Fade fade) {
		if (fade != null) {
			this.fade = Optional.of(fade);
		} else {
			this.fade = Optional.empty();
		}
	}

	/*
	 * Properties
	 */

	public ReadOnlyIntegerProperty indexProperty() {
		return indexProperty;
	}

	public ObjectProperty<State> stateProperty() {
		return stateProperty;
	}

	public StringProperty titleProperty() {
		return titleProperty;
	}

	public ReadOnlyObjectProperty<Duration> currentDurationProperty() {
		if (audioHandlerProperty.get().isMediaLoaded(this))
			return audioHandlerProperty.get().positionProperty(this);
		else
			return null;
	}

	public boolean hasMedia() {
		return getFile() != null;
	}

	@Override
	public int compareTo(Pad o) {
		return Integer.compare(getIndex(), o.getIndex());
	}

	@Override
	public String toString() {
		return getIndex() + ": " + getTitle();
	}

	public String toPrettyString() {
		return (getIndex() + 1) + ": " + getTitle();
	}

	public boolean isEof() {
		return isEof;
	}

	public boolean isVideo() {
		for (String extension : PlayPadPlugin.getImplementation().getVideoFiles()) {
			if (pathProperty.get() != null) {
				if (pathProperty.get().matches("." + extension))
					return true;
			}
		}
		return false;
	}

	public IPadViewController getController() {
		return controller;
	}

	public void setController(IPadViewController controller) {
		this.controller = controller;
	}

	public boolean isPadLoaded() {
		if (audioHandlerProperty.get().isMediaLoaded(this)) {
			return true;
		} else {
			for (PadListener listener : PlayPadPlugin.getImplementation().getPadListener()) {
				if (listener.isPadLoaded(this))
					return true;
			}
		}
		return false;
	}

	public void clearMedia() {
		if (getState() == State.PAUSE || getState() == State.PLAY)
			setState(State.STOP);
		audioHandlerProperty.get().unloadMedia(this);
		pathProperty.set(null);
		lastExceptionProperty.set(null);
	}

	public void setEof(boolean eof) {
		this.isEof = eof;
	}

	public void setPlaying(boolean playing) {
		this.isPlaying = playing;
	}

	public ObjectProperty<AudioHandler> audioHandlerProperty() {
		return audioHandlerProperty;
	}

	public Project getProject() {
		return project;
	}

	// Exception
	public void throwException(Exception ex) {
		if (ex != null) {
			PadException padException = new PadException(this, ex);
			lastExceptionProperty.set(padException);
			ex.printStackTrace();
		} else {
			lastExceptionProperty.set(null);
		}
	}

	public PadException getLastException() {
		return lastExceptionProperty.get();
	}

	public ObjectProperty<PadException> lastExceptionProperty() {
		return lastExceptionProperty;
	}

	private double getMasterVolume() {
		return Profile.currentProfile().getProfileSettings().getVolume();
	}

	// Action Methods
	public void play() {
		isEof = false;
		isPlaying = true;

		if (!audioHandlerProperty.get().isMediaLoaded(this)) {
			loadMedia();
		}
		audioHandlerProperty.get().setVolume(this, volumeProperty, getMasterVolume());

		audioHandlerProperty.get().play(this);

		if ((fade.isPresent() && fade.get().getFadeIn().toMillis() > 0.0)
				|| (!fade.isPresent() && Profile.currentProfile().getProfileSettings().getFade().getFadeIn().toSeconds() > 0.0)) {
			fadeIn();
		}

	}

	public void fadeIn() {
		if (transition != null) {
			transition.stop();
		}

		audioHandlerProperty.get().setVolume(this, 0, getMasterVolume());
		transition = new Transition() {

			{
				if (fade.isPresent())
					setCycleDuration(fade.get().getFadeIn());
				else
					setCycleDuration(Profile.currentProfile().getProfileSettings().getFade().getFadeIn());
			}

			@Override
			protected void interpolate(double frac) {
				audioHandlerProperty.get().setVolume(Pad.this, frac * getVolume(), getMasterVolume());
			}
		};
		transition.play();
	}

	public void pause(boolean fadeOut, Runnable onFinish) {
		if (audioHandlerProperty.get().isMediaLoaded(this)) {
			if (fadeOut && (fade.isPresent() && fade.get().getFadeOut().toMillis() > 0
					|| Profile.currentProfile().getProfileSettings().getFade().getFadeOut().toSeconds() > 0)) {
				fadeOut(() ->
				{
					audioHandlerProperty.get().pause(this);
					if (onFinish != null)
						onFinish.run();
				});
			} else {
				audioHandlerProperty.get().pause(this);
				if (onFinish != null)
					onFinish.run();
			}
		}
	}

	public void stop(boolean fadeOut, Runnable onFinish) {
		if (isPlaying == true) {
			if (audioHandlerProperty.get().isMediaLoaded(this)) {
				isPlaying = false;
				if ((fadeOut && !isEof) && (fade.isPresent() && fade.get().getFadeOut().toMillis() > 0
						|| Profile.currentProfile().getProfileSettings().getFade().getFadeOut().toSeconds() > 0)) {
					if (!isEof) { // Nur wenn nicht am Ende vom Media Player
						fadeOut(() ->
						{
							audioHandlerProperty.get().stop(this);
							stateProperty.set(State.READY);
							if (onFinish != null)
								onFinish.run();
						});
						return;
					}
				} else {
					audioHandlerProperty.get().stop(this);
					if (onFinish != null)
						onFinish.run();
					stateProperty.set(State.READY);
				}
			}
		}
	}

	/*
	 * https://gist.github.com/mariuszz/5099053 | https://gist.github.com/james-d/10800593
	 */
	public void fadeOut(Runnable onFinish) {
		if (transition != null) {
			transition.stop();
		}
		transition = new Transition() {

			{
				if (fade.isPresent())
					setCycleDuration(fade.get().getFadeOut());
				else
					setCycleDuration(Profile.currentProfile().getProfileSettings().getFade().getFadeOut());
			}

			@Override
			protected void interpolate(double frac) {
				audioHandlerProperty.get().setVolume(Pad.this, getVolume() - frac * getVolume(), getMasterVolume());
			}
		};
		transition.setOnFinished(event ->
		{
			onFinish.run();
		});
		transition.play();
	}

	// Loader
	public void loadMedia() {
		try {
			audioHandlerProperty.set(AudioRegistry.geAudioType().createAudioHandler());

			// wenn neue Media geladen werden soll, muss der Path temporär gespeichert werden, da in clearMedia gelöscht wird
			String tempPath = pathProperty.get();

			// Altes Löschen
			if (isPadLoaded())
				clearMedia();

			pathProperty.set(tempPath);
			if (pathProperty.get() != null) {
				Path path = Paths.get(URI.create(pathProperty.get()));
				if (Files.notExists(path)) {
					throwException(new FileNotFoundException());
					return;
				}

				// Check File Extension
				if (audioHandlerProperty.get().isMediaSupported(getPath()))
					audioHandlerProperty.get().loadMedia(this);
			}
		} catch (Exception e) {
			throwException(e);
			e.printStackTrace();
		}
	}

	public void clearPad() {
		if (getState() == State.PAUSE || getState() == State.PLAY)
			setState(State.STOP);
		audioHandlerProperty.get().unloadMedia(this);

		PlayPadPlugin.getImplementation().getPadListener().forEach(l -> l.onClear(this));

		titleProperty.set(null);
		pathProperty.set(null);
		lastExceptionProperty.set(null);

		userInfo.clear();

		setState(State.EMPTY);
	}

	public static final boolean padInRange(int page, Pad pad) {
		ProfileSettings profilSettings = Profile.currentProfile().getProfileSettings();

		int currentStartIndex = page * profilSettings.getColumns() * profilSettings.getRows();
		int currentEndIndex = currentStartIndex + profilSettings.getColumns() * profilSettings.getRows() - 1;
		if (pad.getIndex() <= currentEndIndex && pad.getIndex() >= currentStartIndex) {
			return true;
		} else {
			return false;
		}
	}
}