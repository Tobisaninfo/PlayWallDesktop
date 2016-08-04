package de.tobias.playpad;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

import org.dom4j.DocumentException;

import de.tobias.playpad.action.mapper.KeyboardMapperConnect;
import de.tobias.playpad.action.mapper.MapperRegistry;
import de.tobias.playpad.action.mapper.MidiMapperConnect;
import de.tobias.playpad.audio.ClipAudioHandler;
import de.tobias.playpad.audio.JavaFXAudioHandler;
import de.tobias.playpad.audio.TinyAudioHandler;
import de.tobias.playpad.design.modern.ModernGlobalDesign;
import de.tobias.playpad.midi.device.DeviceRegistry;
import de.tobias.playpad.midi.device.PD12;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectReference;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileListener;
import de.tobias.playpad.settings.ProfileReference;
import de.tobias.playpad.update.PlayPadUpdater;
import de.tobias.playpad.update.UpdateRegistery;
import de.tobias.playpad.update.Updates;
import de.tobias.playpad.view.MapperOverviewViewController;
import de.tobias.playpad.viewcontroller.LaunchDialog;
import de.tobias.playpad.viewcontroller.dialog.ChangelogDialog;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.ConsoleUtils;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Localization.LocalizationDelegate;
import de.tobias.utils.util.OS;
import de.tobias.utils.util.OS.OSType;
import de.tobias.utils.util.Worker;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/*
 * TODOS
 */
// PlayWall 5.0.0
// FIXME XML Tags in String Konstanten

// Common Updater für Plugins & Programme
// Changelog OK Button
// Keine Schriftgröße bei Cart Layout

// Profile mit UUID

// Pad System neu machen
// Neue PadViewController für jedes pad
// Midi Modell Überarbeiten
// Bei Seitenwechsel Pad auf Play lassen

// TEST Trigger

// PlayWall 5.1
// FEATURE Global Volume Trigger mit x% und 100%
// FEATURE Option bei Import Media auch Copy Media in Library
// FEATURE lnk für Windows mit Dateiparameter
// FEATURE Backups irgendwann löschen

public class PlayPadMain extends Application implements LocalizationDelegate, ProfileListener {

	private static final String iconPath = "icon_small.png";
	public static Optional<Image> stageIcon = Optional.empty();

	public static final long displayTimeMillis = 1500;

	public static final String projectType = "*.xml";
	public static final String projectZIPType = "*.zip";
	public static final String midiPresetType = "*.pre";

	private static ResourceBundle uiResourceBundle;

	private static PlayPadImpl impl;
	private static PlayPadUpdater updater;

	public static ResourceBundle getUiResourceBundle() {
		return uiResourceBundle;
	}

	public static void main(String[] args) throws Exception {
		// Verhindert einen Bug unter Windows 10 mit comboboxen
		if (OS.getType() == OSType.Windows) {
			System.setProperty("glass.accessible.force", "false");
		}

		// Debug
		System.setOut(ConsoleUtils.convertStream(System.out, "[PlayWall] "));
		System.setErr(ConsoleUtils.convertStream(System.err, "[PlayWall] "));

		App app = ApplicationUtils.registerMainApplication(PlayPadMain.class);
		ApplicationUtils.registerUpdateSercive(new VersionUpdater());
		app.start(args);
	}

	@Override
	public void init() throws Exception {
		impl = new PlayPadImpl();
		PlayPadPlugin.setImplementation(impl);
		PlayPadPlugin.setRegistryCollection(new RegistryCollectionImpl());

		// Localization
		setupLocalization();

		// Console
		if (!ApplicationUtils.getApplication().isDebug()) {
			System.setOut(ConsoleUtils.streamToFile(ApplicationUtils.getApplication().getPath(PathType.LOG, "out.log")));
			System.setErr(ConsoleUtils.streamToFile(ApplicationUtils.getApplication().getPath(PathType.LOG, "err.log")));
		}
	}

	@Override
	public void start(Stage stage) {
		try {
			// Assets
			try {
				Image stageIcon = new Image(iconPath);
				PlayPadMain.stageIcon = Optional.of(stageIcon);
			} catch (Exception e) {}

			/*
			 * Setup
			 */
			updater = new PlayPadUpdater();
			UpdateRegistery.registerUpdateable(updater);
			registerComponents();

			// Load Plugin Path
			Path pluginFolder;
			if (getParameters().getNamed().containsKey("plugin")) {
				pluginFolder = Paths.get(getParameters().getNamed().get("plugin"));
			} else {
				pluginFolder = ApplicationUtils.getApplication().getPath(PathType.LIBRARY);
			}
			setupPlugins(pluginFolder);

			/*
			 * Load Data
			 */
			ProfileReference.loadProfiles();
			ProjectReference.loadProjects();

			// Changelog nach Update anzeigen
			ViewController.create(ChangelogDialog.class);

			// Auto Open Project
			if (getParameters().getRaw().size() > 0) {
				UUID uuid = UUID.fromString(getParameters().getNamed().get("project"));
				impl.openProject(Project.load(ProjectReference.getProject(uuid), true, null));
				return;
			}

			ViewController.create(LaunchDialog.class, stage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() throws Exception {
		try {
			ProfileReference.saveProfiles();
			ProjectReference.saveProjects();
		} catch (Exception e) {
			e.printStackTrace(); // Speichern Fehler
		}

		// Shutdown components
		// TODO use AutoCloseable
		TinyAudioHandler.shutdown();
		ClipAudioHandler.shutdown();

		impl.shutdown();

		Platform.exit();
		System.exit(0);
	}

	private void registerComponents() {
		// Midi
		DeviceRegistry.getFactoryInstance().registerDevice(PD12.NAME, PD12.class);

		// Mapper
		MapperRegistry.registerMapperConnect(new MidiMapperConnect());
		MapperRegistry.registerMapperConnect(new KeyboardMapperConnect());
		MapperRegistry.setOverviewViewController(new MapperOverviewViewController());

		Profile.registerListener(this);

		try {
			// Load Components
			RegistryCollection registryCollection = PlayPadPlugin.getRegistryCollection();

			registryCollection.getActions().loadComponentsFromFile("de/tobias/playpad/components/Actions.xml");
			registryCollection.getAudioHandlers().loadComponentsFromFile("de/tobias/playpad/components/AudioHandler.xml");
			registryCollection.getDragModes().loadComponentsFromFile("de/tobias/playpad/components/DragMode.xml");
			registryCollection.getDesigns().loadComponentsFromFile("de/tobias/playpad/components/Design.xml");
			registryCollection.getMappers().loadComponentsFromFile("de/tobias/playpad/components/Mapper.xml");
			registryCollection.getPadContents().loadComponentsFromFile("de/tobias/playpad/components/PadContent.xml");
			registryCollection.getTriggerItems().loadComponentsFromFile("de/tobias/playpad/components/Trigger.xml");
			registryCollection.getMainLayouts().loadComponentsFromFile("de/tobias/playpad/components/Layout.xml");

			// Set Default
			registryCollection.getAudioHandlers().setDefaultID(JavaFXAudioHandler.NAME);
			registryCollection.getDesigns().setDefaultID(ModernGlobalDesign.TYPE);
		} catch (IllegalAccessException | ClassNotFoundException | InstantiationException | IOException | DocumentException
				| NoSuchComponentException e) {
			e.printStackTrace();
		}
	}

	private void setupPlugins(Path pluginPath) throws IOException, MalformedURLException {
		// Delete old plugins
		impl.deletePlugins();

		// Load Plugins
		impl.loadPlugin(pluginPath.toUri());
	}

	private void setupLocalization() {
		Localization.setDelegate(this);
		Localization.load();

		uiResourceBundle = Localization.loadBundle("de/tobias/playpad/assets/lang/ui", getClass().getClassLoader());
	}

	/**
	 * Handle Auto Update on profile reload
	 */
	@Override
	public void reloadSettings(Profile oldProfile, Profile newProfile) {
		// Update PlayWall
		if (newProfile.getProfileSettings().isAutoUpdate()) {
			Worker.runLater(() ->
			{
				UpdateRegistery.lookupUpdates(newProfile.getProfileSettings().getUpdateChannel());
				if (!UpdateRegistery.getAvailableUpdates().isEmpty()) {
					Platform.runLater(() ->
					{
						Alert alert = new Alert(AlertType.CONFIRMATION);
						alert.setHeaderText(Localization.getString(Strings.UI_Dialog_AutoUpdate_Header));
						alert.setContentText(Localization.getString(Strings.UI_Dialog_AutoUpdate_Content));
						alert.showAndWait().filter(item -> item == ButtonType.OK).ifPresent(result ->
						{
							try {
								Updates.startUpdate();
							} catch (IOException e) {
								e.printStackTrace();
							}
						});
					});
				}
			});
		}
	}

	@Override
	public String getBaseResource() {
		return "de/tobias/playpad/assets/lang/";
	}

	@Override
	public Locale getLocale() {
		return Locale.getDefault();
	}

	/**
	 * Gibt die Implementierung des Peers für Plugins zurück.
	 * 
	 * @return Schnittstelle
	 * 
	 * @see PlayPad
	 */
	public static PlayPadImpl getProgramInstance() {
		return impl;
	}

}