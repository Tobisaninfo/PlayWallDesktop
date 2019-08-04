package de.tobias.playpad;

import de.thecodelabs.logger.FileOutputOption;
import de.thecodelabs.logger.LogLevelFilter;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.storage.settings.UserDefaults;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.application.system.NativeApplication;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.Localization.LocalizationDelegate;
import de.thecodelabs.utils.util.OS;
import de.thecodelabs.utils.util.OS.OSType;
import de.thecodelabs.utils.util.SystemUtils;
import de.thecodelabs.versionizer.service.UpdateService;
import de.tobias.playpad.design.ModernStyleableImpl;
import de.tobias.playpad.plugin.ModernPluginManager;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.loader.ProjectLoader;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.server.ServerHandlerImpl;
import de.tobias.playpad.server.sync.command.CommandExecutorHandlerImpl;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.update.VersionUpdater;
import de.tobias.playpad.util.UUIDSerializer;
import de.tobias.playpad.viewcontroller.LaunchDialog;
import de.tobias.playpad.viewcontroller.LoginViewController;
import de.tobias.playpad.viewcontroller.dialog.AutoUpdateDialog;
import io.github.openunirest.http.Unirest;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.dom4j.DocumentException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.Locale;
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

	private static final String ICON_PATH = "icon_small.png";
	public static Image stageIcon;

	public static final long displayTimeMillis = 1500;

	public static final String projectZIPType = "*.zip";
	public static final String midiPresetType = "*.pre";

	private static PlayPadImpl impl;
	public static SSLContext sslContext;

	public static void main(String[] args) {
		// Verhindert einen Bug unter Windows 10 mit comboboxen
		if (OS.getType() == OSType.Windows) {
			System.setProperty("glass.accessible.force", "false");
		}

		// Register UserDefaults Serializer
		UserDefaults.registerLoader(new UUIDSerializer(), UUID.class);

		ApplicationUtils.addAppListener(PlayPadMain::applicationWillStart);
		App app = ApplicationUtils.registerMainApplication(PlayPadMain.class);
		ApplicationUtils.registerUpdateSercive(new VersionUpdater());

		app.start(args);
	}

	private static void applicationWillStart(App app)
	{
		Logger.init(app.getPath(PathType.LOG));
		if(app.isDebug())
		{
			Logger.setLevelFilter(LogLevelFilter.DEBUG);
			Logger.setFileOutput(FileOutputOption.DISABLED);
		}
		else
		{
			Logger.setFileOutput(FileOutputOption.COMBINED);
		}
		Logger.info("Logging initialized (Running in LogLevel: {0})", Logger.getLevelFilter().toString());
	}


	@Override
	public void init() {
		App app = ApplicationUtils.getApplication();

		if (!app.isDebug()) {
			Logger.setFileOutput(FileOutputOption.COMBINED);
		}

		// Init SSLContext
		if (app.isDebug()) {
			disableSSL();
		}

		Logger.info("Run Path: {0}", SystemUtils.getRunPath());

		// Localization
		setupLocalization();

		// Setup Global Settings
		Logger.debug("Load global settings");
		Path globalSettingsPath = app.getPath(PathType.CONFIGURATION, "GlobalSettings.xml");
		GlobalSettings globalSettings = GlobalSettings.load(globalSettingsPath);
		globalSettings.getKeyCollection().loadDefaultFromFile("components/Keys.xml", Localization.getBundle());
		globalSettings.getKeyCollection().load(globalSettingsPath);

		// Setup Profiles
		try {
			ProfileReferenceManager.loadProfiles();
		} catch (IOException | DocumentException e) {
			Logger.error(e);
		}

		// Set Factory Implementations
		impl = new PlayPadImpl(globalSettings, getParameters());
		PlayPadPlugin.setInstance(impl);
		PlayPadPlugin.setStyleable(new ModernStyleableImpl());
		PlayPadPlugin.setRegistryCollection(new RegistryCollectionImpl());
		PlayPadPlugin.setServerHandler(new ServerHandlerImpl());
		PlayPadPlugin.setCommandExecutorHandler(new CommandExecutorHandlerImpl());

		/*
		 * Setup
		 */

		impl.startup(Localization.getBundle(), new LoginViewController());
	}

	@Override
	public void start(Stage stage) {
		try {
			// Assets
			PlayPadMain.stageIcon = new Image(ICON_PATH);
			Alerts.getInstance().setDefaultIcon(stageIcon);

			// App Setup
			NativeApplication.sharedInstance().setDockIcon(new Image("gfx/Logo-large.png"));
			NativeApplication.sharedInstance().setAppearance(true);

			try {
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
			} catch (Exception e) {
				Logger.error("Unable to load plugins");
				Logger.error(e);
			}

			/*
			 * Load Projects
			 */
			try {
				ProjectReferenceManager.loadProjects();
			} catch (IOException e) {
				Logger.error(e);
			}

			if (PlayPadPlugin.getInstance().getGlobalSettings().isOpenLastDocument()) {
				UUID value = (UUID) ApplicationUtils.getApplication().getUserDefaults().getData("project");
				if (value != null) {
					ProjectLoader loader = new ProjectLoader(ProjectReferenceManager.getProject(value));
					// TODO Load indicator
					Project project = loader.load();
					impl.openProject(project, null);
					return;
				}
			}

			// Auto Open Project DEBUG
			if (!getParameters().getRaw().isEmpty()) {
				if (getParameters().getNamed().containsKey("project")) {
					UUID uuid = UUID.fromString(getParameters().getNamed().get("project"));
					ProjectLoader loader = new ProjectLoader(ProjectReferenceManager.getProject(uuid));
					Project project = loader.load();
					impl.openProject(project, null);
					return;
				}
			}

			// Show Launch Stage
			new LaunchDialog(stage);

			checkUpdates(impl.globalSettings, stage);
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	@SuppressWarnings("Duplicates")
	private void checkUpdates(GlobalSettings globalSettings, Window owner) {
		if (globalSettings.isAutoUpdate() && !globalSettings.isIgnoreUpdate()) {
			Worker.runLater(() ->
			{
				UpdateService updateService = impl.getUpdateService();
				updateService.fetchCurrentVersion();
				if (updateService.isUpdateAvailable()) {
					Platform.runLater(() ->
					{
						AutoUpdateDialog autoUpdateDialog = new AutoUpdateDialog(updateService, owner);
						autoUpdateDialog.showAndWait().filter(item -> item.getButtonData() == ButtonData.APPLY).ifPresent(result ->
						{
							Logger.info("Install update");
							try {
								updateService.runVersionizerInstance(updateService.getAllLatestVersionEntries());
								System.exit(0);
							} catch (IOException e) {
								Logger.error(e);
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
	public void stop() {
		try {
			// Save last open project
			Project project = impl.getCurrentProject();
			if (project != null) {
				ApplicationUtils.getApplication().getUserDefaults().setData("project", project.getProjectReference().getUuid());
			}


			ProfileReferenceManager.saveProfiles();
			ProjectReferenceManager.saveProjects();
			impl.getGlobalSettings().save();
		} catch (Exception e) {
			Logger.error(e);
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
	}

	@Override
	public String[] getBaseResources() {
		return new String[]{
				"lang/",
				"lang/ui"
		};
	}

	@Override
	public boolean useMultipleResourceBundles() {
		return true;
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

	private static void disableSSL() {
		Logger.warning("Setup TrustManager in Debug Mode");
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

		try {
			// Install the all-trusting trust manager
			sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> true);

			// Unirest
			SSLContext sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
			Unirest.setHttpClient(httpclient);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}