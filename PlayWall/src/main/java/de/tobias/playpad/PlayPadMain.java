package de.tobias.playpad;

import de.thecodelabs.logger.FileOutputOption;
import de.thecodelabs.logger.LogLevelFilter;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.storage.proxy.SettingsProxy;
import de.thecodelabs.storage.settings.UserDefaults;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.util.OS;
import de.thecodelabs.utils.util.OS.OSType;
import de.thecodelabs.utils.util.SystemUtils;
import de.tobias.playpad.design.ModernDesignProviderImpl;
import de.tobias.playpad.design.ModernStyleableImpl;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.update.VersionUpdater;
import de.tobias.playpad.util.ListSerializer;
import de.tobias.playpad.util.UUIDSerializer;
import de.tobias.playpad.viewcontroller.SplashScreenViewController;
import io.github.openunirest.http.Unirest;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.UUID;

/*
 * TODOS
 */
// Idden
// FEATURE Option bei Import Media auch Copy Media in Library
// FEATURE lnk für Windows mit Dateiparameter

public class PlayPadMain extends Application {

	private static final String ICON_PATH = "icon_small.png";

	public static final long NOTIFICATION_DISPLAY_TIME = 1500;

	public static final String ZIP_TYPE = "*.zip";
	public static final String PRESET_TYPE = "*.pre";

	private static PlayPadImpl impl;
	public static SSLContext sslContext;

	public static void main(String[] args) {
		// Verhindert einen Bug unter Windows 10 mit comboboxen
		if (OS.getType() == OSType.Windows) {
			System.setProperty("glass.accessible.force", "false");
		}

		// Register UserDefaults Serializer
		UserDefaults.registerLoader(new UUIDSerializer(), UUID.class);
		UserDefaults.registerLoader(new ListSerializer(), ArrayList.class);

		ApplicationUtils.addAppListener(PlayPadMain::applicationWillStart);
		App app = ApplicationUtils.registerMainApplication(PlayPadMain.class);
		ApplicationUtils.registerUpdateSercive(new VersionUpdater());

		app.start(args);
	}

	private static void applicationWillStart(App app) {
		Logger.init(app.getPath(PathType.LOG));
		if (app.isDebug()) {
			Logger.setLevelFilter(LogLevelFilter.DEBUG);
			Logger.setFileOutput(FileOutputOption.DISABLED);
			Logger.addFilter(message -> !message.getCaller().getClassName().contains("org.apache.commons.logging.impl.SLF4JLog"));
		} else {
			Logger.setFileOutput(FileOutputOption.COMBINED);
		}
		Logger.info("Logging initialized (Running in LogLevel: {0})", Logger.getLevelFilter().toString());
	}


	@Override
	public void init() {
		App app = ApplicationUtils.getApplication();
		Logger.info("Running on Java: " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
		Logger.info("Run Path: {0}", SystemUtils.getRunPath());

		// Init SSLContext
		if (app.isDebug()) {
			disableSSL();
		}

		// Set Factory Implementations
		impl = new PlayPadImpl(getParameters());

		Image stageIcon = new Image(ICON_PATH);
		Alerts.getInstance().setDefaultIcon(stageIcon);
		impl.setIcon(stageIcon, getClass().getClassLoader().getResourceAsStream(ICON_PATH));

		PlayPadPlugin.setStyleable(new ModernStyleableImpl());
		impl.setModernDesign(new ModernDesignProviderImpl());

		PlayPadPlugin.setInstance(impl);
	}

	@Override
	public void start(Stage stage) {
		SplashScreenViewController.show(impl, stage);
	}

	@Override
	public void stop() {
		try {
			// Save last opened project
			Project project = impl.getCurrentProject();
			if (project != null) {
				ApplicationUtils.getApplication().getUserDefaults()
						.setData("project", project.getProjectReference().getUuid());
			}

			ProfileReferenceManager.saveProfiles();
			ProjectReferenceManager.saveProjects();
			impl.getGlobalSettings().save();
			SettingsProxy.saveAll();
		} catch (Exception e) {
			Logger.error(e);
		}

		impl.shutdown();

		Platform.exit();
		System.exit(0);
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
			Logger.error(e);
		}
	}
}