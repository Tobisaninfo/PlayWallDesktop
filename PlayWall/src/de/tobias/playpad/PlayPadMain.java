package de.tobias.playpad;

import de.tobias.playpad.plugin.ModernPluginManager;
import de.tobias.playpad.profile.ref.ProfileReferences;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSerializer;
import de.tobias.playpad.project.ref.ProjectReferences;
import de.tobias.playpad.server.ServerHandlerImpl;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.update.PlayPadUpdater;
import de.tobias.playpad.update.Updates;
import de.tobias.playpad.viewcontroller.LaunchDialog;
import de.tobias.playpad.viewcontroller.dialog.AutoUpdateDialog;
import de.tobias.updater.client.UpdateRegistery;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.ConsoleUtils;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Localization.LocalizationDelegate;
import de.tobias.utils.util.OS;
import de.tobias.utils.util.OS.OSType;
import de.tobias.utils.util.Worker;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

/*
 * TODOS
 */
// Idden
// FEATURE Global Volume Trigger mit x% und 100%
// FEATURE Option bei Import Media auch Copy Media in Library
// FEATURE lnk für Windows mit Dateiparameter
// FEATURE Backups irgendwann löschen

public class PlayPadMain extends Application implements LocalizationDelegate {

	private static final String iconPath = "icon_small.png";
	public static Optional<Image> stageIcon = Optional.empty();

	public static final long displayTimeMillis = 1500;

	public static final String projectType = "*.xml";
	public static final String projectZIPType = "*.zip";
	public static final String midiPresetType = "*.pre";

	private static ResourceBundle uiResourceBundle;

	private static PlayPadImpl impl;

	public static ResourceBundle getUiResourceBundle() {
		return uiResourceBundle;
	}

	public static SSLContext sslContext;

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
		App app = ApplicationUtils.getApplication();

		// Init SSLContext
		if (app.isDebug()) {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			}};

			// Install the all-trusting trust manager
			sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> hostname.equals("localhost"));
		}

		// Localization
		setupLocalization();

		// Setup Global Settings
		Path globalSettingsPath = app.getPath(PathType.CONFIGURATION, "GlobalSettings.xml");
		GlobalSettings globalSettings = GlobalSettings.load(globalSettingsPath);
		globalSettings.getKeyCollection().loadDefaultFromFile("de/tobias/playpad/components/Keys.xml", uiResourceBundle);
		globalSettings.getKeyCollection().load(globalSettingsPath);

		impl = new PlayPadImpl(globalSettings, getParameters());
		PlayPadPlugin.setImplementation(impl);
		PlayPadPlugin.setRegistryCollection(new RegistryCollectionImpl());
		PlayPadPlugin.setServerHandler(new ServerHandlerImpl());

		// Console
		if (!app.isDebug()) {
			System.setOut(ConsoleUtils.streamToFile(app.getPath(PathType.LOG, "out.log")));
			System.setErr(ConsoleUtils.streamToFile(app.getPath(PathType.LOG, "err.log")));
		}
	}

	@Override
	public void start(Stage stage) {
		try {
			// Assets
			try {
				Image stageIcon = new Image(iconPath);
				PlayPadMain.stageIcon = Optional.of(stageIcon);
			} catch (Exception ignored) {
			}

			/*
			 * Setup
			 */
			PlayPadUpdater updater = new PlayPadUpdater();
			UpdateRegistery.registerUpdateable(updater);

			impl.startup(Localization.getBundle());

			// Load Plugin Path
			if (!getParameters().getRaw().contains("noplugins")) {
				Path pluginFolder;
				if (getParameters().getNamed().containsKey("plugin")) {
					String pluginParam = getParameters().getNamed().get("plugin");
					for (String part : pluginParam.split(":")) {
						pluginFolder = Paths.get(part);
						setupPlugins(pluginFolder);
					}
				} else {
					pluginFolder = ApplicationUtils.getApplication().getPath(PathType.LIBRARY);
					setupPlugins(pluginFolder);
				}
			}

			/*
			 * Load Data
			 */
			ProfileReferences.loadProfiles();
			ProjectReferences.loadProjects();

			// Auto Open Project
			if (getParameters().getRaw().size() > 0) {
				if (getParameters().getNamed().containsKey("project")) {
					UUID uuid = UUID.fromString(getParameters().getNamed().get("project"));
					Project project = ProjectSerializer.load(ProjectReferences.getProject(uuid), true, null);
					impl.openProject(project, null);
					return;
				}
			}

			// Show Launch Stage
			new LaunchDialog(stage);

			// Check Updates
			checkUpdates(impl.globalSettings, stage);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkUpdates(GlobalSettings globalSettings, Window owner) {
		if (globalSettings.isAutoUpdate() && !globalSettings.isIgnoreUpdate()) {
			Worker.runLater(() ->
			{
				UpdateRegistery.lookupUpdates(globalSettings.getUpdateChannel());
				if (!UpdateRegistery.getAvailableUpdates().isEmpty()) {
					Platform.runLater(() ->
					{
						AutoUpdateDialog autoUpdateDialog = new AutoUpdateDialog(owner);
						autoUpdateDialog.showAndWait().filter(item -> item.getButtonData() == ButtonData.APPLY).ifPresent(result ->
						{
							try {
								Updates.startUpdate();
							} catch (IOException e) {
								e.printStackTrace();
							}
						});
						if (autoUpdateDialog.isSelected()) {
							globalSettings.setIgnoreUpdate(true);
						}
					});
				}
			});
		}
	}

	@Override
	public void stop() throws Exception {
		try {
			ProfileReferences.saveProfiles();
			ProjectReferences.saveProjects();
			impl.getGlobalSettings().save();
		} catch (Exception e) {
			e.printStackTrace(); // Speichern Fehler
		}

		impl.shutdown();

		Platform.exit();
		System.exit(0);
	}

	private void setupPlugins(Path pluginPath) throws IOException {
		// Delete old plugins
		ModernPluginManager.getInstance().deletePlugins();

		// Load Plugins
		ModernPluginManager.getInstance().loadFile(pluginPath);
	}

	private void setupLocalization() {
		Localization.setDelegate(this);
		Localization.load();

		uiResourceBundle = Localization.loadBundle("de/tobias/playpad/assets/lang/ui", getClass().getClassLoader());
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
	 * @see PlayPad
	 */
	public static PlayPadImpl getProgramInstance() {
		return impl;
	}

}