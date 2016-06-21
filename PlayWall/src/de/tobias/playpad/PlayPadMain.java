package de.tobias.playpad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;

import de.tobias.playpad.action.ActionRegistery;
import de.tobias.playpad.action.connect.CartActionConnect;
import de.tobias.playpad.action.connect.NavigateActionConnect;
import de.tobias.playpad.action.connect.PageActionConnect;
import de.tobias.playpad.action.mapper.KeyboardMapperConnect;
import de.tobias.playpad.action.mapper.MapperRegistry;
import de.tobias.playpad.action.mapper.MidiMapperConnect;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.audio.ClipAudioHandler;
import de.tobias.playpad.audio.ClipAudioHandlerConnect;
import de.tobias.playpad.audio.JavaFXAudioHandler;
import de.tobias.playpad.audio.JavaFXHandlerConnect;
import de.tobias.playpad.audio.TinyAudioHandler;
import de.tobias.playpad.audio.TinyAudioHandlerConnect;
import de.tobias.playpad.layout.LayoutRegistry;
import de.tobias.playpad.layout.classic.ClassicGlobalLayout;
import de.tobias.playpad.layout.classic.ClassicLayoutConnect;
import de.tobias.playpad.layout.modern.ModernLayoutConnect;
import de.tobias.playpad.midi.device.DeviceRegistry;
import de.tobias.playpad.midi.device.PD12;
import de.tobias.playpad.pad.conntent.PadContentRegistry;
import de.tobias.playpad.pad.content.AudioContentConnect;
import de.tobias.playpad.pad.drag.MoveDragMode;
import de.tobias.playpad.pad.drag.PadDragModeRegistery;
import de.tobias.playpad.pad.drag.ReplaceDragMode;
import de.tobias.playpad.plugin.PadListener;
import de.tobias.playpad.plugin.SettingsListener;
import de.tobias.playpad.plugin.WindowListener;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectReference;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileListener;
import de.tobias.playpad.settings.ProfileReference;
import de.tobias.playpad.tigger.TriggerRegistry;
import de.tobias.playpad.trigger.CartTriggerItemConnect;
import de.tobias.playpad.trigger.VolumeTriggerItemConnect;
import de.tobias.playpad.view.MapperOverviewViewController;
import de.tobias.playpad.viewcontroller.IPadSettingsViewController;
import de.tobias.playpad.viewcontroller.ISettingsViewController;
import de.tobias.playpad.viewcontroller.LaunchDialog;
import de.tobias.playpad.viewcontroller.dialog.ChangelogDialog;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MainViewController;
import de.tobias.playpad.viewcontroller.option.UpdateTabViewController;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.ConsoleUtils;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Localization.LocalizationDelegate;
import de.tobias.utils.util.Worker;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;

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

public class PlayPadMain extends Application implements LocalizationDelegate, PlayPad, ProfileListener {

	private static final String PLUGIN_INFO_TXT = "pluginInfo.txt";
	private static final String iconPath = "icon_small.png";
	public static Optional<Image> stageIcon = Optional.empty();

	public static final long displayTimeMillis = 1500;

	public static final String[] projectType = { "*.xml" };
	public static final String[] projectZIPType = { "*.zip" };
	public static final String[] midiPresetType = { "*.pre" };

	private static ResourceBundle uiResourceBundle;
	private static MainViewController mainViewController;

	private static PlayPadUpdater updater;
	private static Set<Path> deletedPlugins = new HashSet<>();

	public static void addDeletedPlugin(Path path) {
		deletedPlugins.add(path);
	}

	/*
	 * 
	 */

	public static ResourceBundle getUiResourceBundle() {
		return uiResourceBundle;
	}

	public static void main(String[] args) throws Exception {
		// Debug
		System.setOut(ConsoleUtils.convertStream(System.out, "[PlayWall] "));
		System.setErr(ConsoleUtils.convertStream(System.err, "[PlayWall] "));

		App app = ApplicationUtils.registerMainApplication(PlayPadMain.class);
		ApplicationUtils.registerUpdateSercive(new VersionUpdater());
		app.start(args);
	}

	private static PluginManager pluginManager;

	@Override
	public void init() throws Exception {
		PlayPadPlugin.setImplementation(this);

		// Localization
		setupLocalization();

		// Console
		if (!ApplicationUtils.getApplication().isDebug()) {
			System.setOut(ConsoleUtils.streamToFile(ApplicationUtils.getApplication().getPath(PathType.LOG, "out.log")));
			System.setErr(ConsoleUtils.streamToFile(ApplicationUtils.getApplication().getPath(PathType.LOG, "err.log")));
		}
	}

	@Override
	public void start(Stage stage) throws Exception {
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
		setupPlugins();

		/*
		 * Load Data
		 */
		ProfileReference.loadProfiles();
		ProjectReference.loadProjects();

		// Changelog nach Update anzeigen
		try {
			ViewController.create(ChangelogDialog.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Auto Open Project
		if (getParameters().getRaw().size() > 0) {
			try {
				UUID uuid = UUID.fromString(getParameters().getNamed().get("project"));
				launchProject(Project.load(ProjectReference.getProject(uuid), true, null));
				return;
			} catch (IllegalArgumentException | NullPointerException e) {} catch (Exception e) {
				e.printStackTrace();
			}
		}

		ViewController.create(LaunchDialog.class, stage);

	}

	@Override
	public void stop() throws Exception {
		try {
			ProfileReference.saveProfiles();
			ProjectReference.saveProjects();
		} catch (Exception e) {
			e.printStackTrace(); // Speichern Fehler
		}

		TinyAudioHandler.shutdown();
		ClipAudioHandler.shutdown();

		pluginManager.shutdown();

		Path pluginInfoPath = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, PLUGIN_INFO_TXT);
		try {
			if (Files.notExists(pluginInfoPath)) {
				Files.createDirectories(pluginInfoPath.getParent());
				Files.createFile(pluginInfoPath);
			}
			PrintWriter deleteWriter = new PrintWriter(Files.newOutputStream(pluginInfoPath));
			for (Path path : deletedPlugins) {
				deleteWriter.println(path.toString());
			}
			deleteWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Worker.shutdown();
		Platform.exit();
		System.exit(0);
	}

	private void registerComponents() {
		// Audio
		AudioRegistry.register(new JavaFXHandlerConnect(), JavaFXAudioHandler.NAME);
		AudioRegistry.register(new TinyAudioHandlerConnect(), TinyAudioHandler.NAME);
		AudioRegistry.register(new ClipAudioHandlerConnect(), ClipAudioHandler.NAME);

		AudioRegistry.setDefaultAudioInterface(JavaFXAudioHandler.NAME);

		// Layout
		LayoutRegistry.registerLayout(new ClassicLayoutConnect());
		LayoutRegistry.registerLayout(new ModernLayoutConnect());
		LayoutRegistry.setDefaultLayout(ClassicGlobalLayout.TYPE);

		// Midi
		DeviceRegistry.getFactoryInstance().registerDevice(PD12.NAME, PD12.class);

		// Trigger
		TriggerRegistry.register(new CartTriggerItemConnect());
		TriggerRegistry.register(new VolumeTriggerItemConnect());

		// Actions
		ActionRegistery.registerActionConnect(new CartActionConnect());
		ActionRegistery.registerActionConnect(new PageActionConnect());
		ActionRegistery.registerActionConnect(new NavigateActionConnect());

		// Mapper
		MapperRegistry.registerMapperConnect(new MidiMapperConnect());
		MapperRegistry.registerMapperConnect(new KeyboardMapperConnect());
		MapperRegistry.setOverviewViewController(new MapperOverviewViewController());

		// Pad Content
		PadContentRegistry.registerActionConnect(new AudioContentConnect());

		// Pad Drag Mode
		PadDragModeRegistery.registerActionConnect(new MoveDragMode());
		PadDragModeRegistery.registerActionConnect(new ReplaceDragMode());

		Profile.registerListener(this);
	}

	private void setupPlugins() throws IOException, MalformedURLException {
		/*
		 * Plugins
		 */
		Path pluginInfoPath = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, PLUGIN_INFO_TXT);

		// Delete Plugin
		if (Files.exists(pluginInfoPath)) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(pluginInfoPath)));
			String line;
			while ((line = reader.readLine()) != null) {
				Path plugin = Paths.get(line);
				Files.deleteIfExists(plugin);
			}
			reader.close();
			Files.delete(pluginInfoPath);
		}

		// Load Plugins
		pluginManager = PluginManagerFactory.createPluginManager();
		if (ApplicationUtils.getApplication().isDebug())
			// DEBUG PLUGINS EINBINDEN
			pluginManager.addPluginsFrom(Paths.get("/Users/tobias/Documents/Programmieren/Java/eclipse/PlayWallPlugins/bin/").toUri());
		else
			pluginManager.addPluginsFrom(ApplicationUtils.getApplication().getPath(PathType.LIBRARY).toUri());
	}

	private void setupLocalization() {
		Localization.setDelegate(this);
		Localization.load();

		uiResourceBundle = Localization.loadBundle("de/tobias/playpad/assets/lang/ui", getClass().getClassLoader());
	}

	public static void launchProject(Project project) {
		mainViewController = new MainViewController(project, mainViewListeners, pluginManager);
	}

	@Override
	public void reloadSettings(Profile oldProfile, Profile currentProfile) {
		// Update PlayWall
		if (currentProfile.getProfileSettings().isAutoUpdate()) {
			Worker.runLater(() ->
			{
				UpdateRegistery.lookupUpdates();
				if (!UpdateRegistery.getAvailableUpdates().isEmpty()) {
					Platform.runLater(() ->
					{
						Alert alert = new Alert(AlertType.CONFIRMATION);
						alert.setHeaderText(Localization.getString(Strings.UI_Dialog_AutoUpdate_Header));
						alert.setContentText(Localization.getString(Strings.UI_Dialog_AutoUpdate_Content));
						alert.showAndWait().filter(item -> item == ButtonType.OK).ifPresent(result ->
						{
							UpdateTabViewController.update(null);
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

	/*
	 * Plugins
	 */
	protected static List<WindowListener<IMainViewController>> mainViewListeners = new ArrayList<>();
	protected static List<WindowListener<ISettingsViewController>> settingsViewListeners = new ArrayList<>();
	protected static List<WindowListener<IPadSettingsViewController>> padSettingsViewListeners = new ArrayList<>();
	protected static List<SettingsListener> settingsListeners = new ArrayList<>();
	protected static List<PadListener> padListeners = new ArrayList<>();

	@Override
	public void addMainViewListener(WindowListener<IMainViewController> listener) {
		mainViewListeners.add(listener);
	}

	@Override
	public void removeMainViewListener(WindowListener<IMainViewController> listener) {
		mainViewListeners.remove(listener);
	}

	@Override
	public void addSettingsViewListener(WindowListener<ISettingsViewController> listener) {
		settingsViewListeners.add(listener);
	}

	@Override
	public void removeSettingsViewListener(WindowListener<ISettingsViewController> listener) {
		settingsViewListeners.remove(listener);
	}

	@Override
	public List<WindowListener<ISettingsViewController>> getSettingsViewListener() {
		return settingsViewListeners;
	}

	@Override
	public void addPadSettingsViewListener(WindowListener<IPadSettingsViewController> listener) {
		padSettingsViewListeners.add(listener);
	}

	@Override
	public void removePadSettingsViewListener(WindowListener<IPadSettingsViewController> listener) {
		padSettingsViewListeners.remove(listener);
	}

	@Override
	public List<WindowListener<IPadSettingsViewController>> getPadSettingsViewListener() {
		return padSettingsViewListeners;
	}

	@Override
	public void addSettingsListener(SettingsListener listener) {
		settingsListeners.add(listener);
	}

	@Override
	public void removeSettingsListener(SettingsListener listener) {
		settingsListeners.remove(listener);
	}

	@Override
	public List<SettingsListener> getSettingsListener() {
		return settingsListeners;
	}

	@Override
	public void addPadListener(PadListener listener) {
		padListeners.add(listener);
	}

	@Override
	public void removePadListener(PadListener listener) {
		padListeners.remove(listener);
	}

	@Override
	public List<PadListener> getPadListener() {
		return padListeners;
	}

	@Override
	public IMainViewController getMainViewController() {
		return mainViewController;
	}

	@Override
	public PluginManager getPluginManager() {
		return pluginManager;
	}

	@Override
	public String[] getProjectFiles() {
		return projectType;
	}

	@Override
	public Optional<Image> getIcon() {
		return stageIcon;
	}

	public static Updatable getUpdater() {
		return updater;
	}

}
