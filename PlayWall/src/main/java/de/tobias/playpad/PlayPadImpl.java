package de.tobias.playpad;

import com.neovisionaries.ws.client.WebSocketException;
import de.thecodelabs.logger.LogLevel;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.midi.action.ActionKeyHandler;
import de.thecodelabs.midi.action.ActionRegistry;
import de.thecodelabs.storage.settings.StorageTypes;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.io.FileUtils;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.SystemUtils;
import de.thecodelabs.versionizer.VersionizerItem;
import de.thecodelabs.versionizer.config.Artifact;
import de.thecodelabs.versionizer.config.Repository;
import de.thecodelabs.versionizer.service.UpdateService;
import de.tobias.playpad.audio.JavaFXHandlerFactory;
import de.tobias.playpad.design.ModernDesign;
import de.tobias.playpad.design.ModernDesignHandlerImpl;
import de.tobias.playpad.log.LogSeasons;
import de.tobias.playpad.log.storage.SqlLiteLogSeasonStorageHandler;
import de.tobias.playpad.plugin.*;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.*;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.view.MapperListViewControllerImpl;
import de.tobias.playpad.viewcontroller.BaseMapperListViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MainViewController;
import de.tobias.playpad.volume.GlobalVolume;
import de.tobias.playpad.volume.PadVolume;
import de.tobias.playpad.volume.VolumeManager;
import javafx.application.Application;
import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class PlayPadImpl implements PlayPad {

	private Application.Parameters parameters;

	protected List<WindowListener<IMainViewController>> mainViewListeners = new ArrayList<>();
	protected List<SettingsListener> settingsListeners = new ArrayList<>();
	protected List<PadListener> padListeners = new ArrayList<>();

	private MainViewController mainViewController;
	private Project currentProject;
	private static Module module;

	private UpdateService updateService;

	protected GlobalSettings globalSettings;
	private ModernDesign modernDesign;

	protected Session session;

	PlayPadImpl(GlobalSettings globalSettings, Application.Parameters parameters) {
		this.parameters = parameters;
		this.globalSettings = globalSettings;

		App app = ApplicationUtils.getApplication();
		module = new Module(app.getInfo().getName(), app.getInfo().getIdentifier());
		ModernPluginManager.getInstance().addModule(module);
	}

	@Override
	public void addMainViewListener(WindowListener<IMainViewController> listener) {
		mainViewListeners.add(listener);
	}

	public List<WindowListener<IMainViewController>> getMainViewListeners() {
		return mainViewListeners;
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
	public Optional<Image> getIcon() {
		return PlayPadMain.stageIcon;
	}

	@Override
	public void shutdown() {
		// Shutdown components
		PlayPadPlugin.getRegistries().getAudioHandlers().getComponents().forEach(i ->
		{
			if (i instanceof AutoCloseable) {
				try {
					((AutoCloseable) i).close();
				} catch (Exception e) {
					Logger.error(e);
				}
			}
		});

		final Server server = PlayPadPlugin.getServerHandler().getServer();
		if (server.getConnectionState() != ConnectionState.DISCONNECTED) {
			server.disconnect();
		}

		try {
			LogSeasons.getStorageHandler().close();
		} catch (RuntimeException e) {
			Logger.log(LogLevel.ERROR, "Cannot close LogSeasonStorageHandler (" + e.getLocalizedMessage() + ")");
		}

		try {
			Path applicationSupportPath = SystemUtils.getApplicationSupportDirectoryPath("de.tobias.playpad.PlayPadMain");
			FileUtils.deleteDirectory(applicationSupportPath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ModernPluginManager.getInstance().showdown();
		Worker.shutdown();
	}

	@Override
	public GlobalSettings getGlobalSettings() {
		return globalSettings;
	}

	public void openProject(Project project, Consumer<NVC> onLoaded) {
		if (mainViewController == null) {
			mainViewController = new MainViewController(e -> {
				currentProject = project;
				mainViewController.openProject(project);
				if (onLoaded != null) {
					onLoaded.accept(e);
				}
				mainViewListeners.forEach(l -> l.onInit(mainViewController));
			});
		} else {
			currentProject = project;
			mainViewController.openProject(project);
		}
	}

	@Override
	public Project getCurrentProject() {
		return currentProject;
	}

	void startup(ResourceBundle resourceBundle, SessionDelegate delegate) {
		App app = ApplicationUtils.getApplication();
		// Setup PlayoutLog
		try {
			Path playOutLogPath = app.getPath(PathType.DOCUMENTS, "logging.db");
			LogSeasons.setStorageHandler(new SqlLiteLogSeasonStorageHandler(playOutLogPath));
			Logger.info("Setup LogSeasonStorageHandler in path: " + playOutLogPath);
		} catch (SQLException e) {
			Logger.error("Cannot setup LogSeasonStorageHandler (" + e.getLocalizedMessage() + ")");
		}

		modernDesign = new ModernDesignHandlerImpl();

		// Register Update Service
		Artifact playpadArtifact = app.getClasspathResource("build-app.json").deserialize(StorageTypes.JSON, Artifact.class);
		Repository repository = app.getClasspathResource("repository.yml").deserialize(StorageTypes.YAML, Repository.class);

		VersionizerItem versionizerItem = new VersionizerItem(repository, SystemUtils.getRunPath().toString());
		updateService = UpdateService.startVersionizer(versionizerItem, UpdateService.Strategy.JAR, UpdateService.InteractionType.GUI);
		updateService.addArtifact(playpadArtifact, SystemUtils.getRunPath());
		updateService.setRepositoryType(globalSettings.getUpdateChannel());

		registerComponents(resourceBundle);
		configureServer(delegate);
	}

	private void registerComponents(ResourceBundle resourceBundle) {
		// Midi
		// TODO Handle PD12
		//DeviceRegistry.getFactoryInstance().registerDevice(PD12.NAME, PD12.class);

		try {
			// Load Components
			Registries registryCollection = PlayPadPlugin.getRegistries();

			registryCollection.getActions().loadComponentsFromFile("components/Actions.xml", module, resourceBundle);
			registryCollection.getAudioHandlers().loadComponentsFromFile("components/AudioHandler.xml", module, resourceBundle);
			registryCollection.getDragModes().loadComponentsFromFile("components/DragMode.xml", module, resourceBundle);
			registryCollection.getPadContents().loadComponentsFromFile("components/PadContent.xml", module, resourceBundle);
			registryCollection.getTriggerItems().loadComponentsFromFile("components/Trigger.xml", module, resourceBundle);
			registryCollection.getMainLayouts().loadComponentsFromFile("components/Layout.xml", module, resourceBundle);

			// Register Handlers in Midi Library
			ActionKeyHandler.setRunOnFxThread(true);
			registryCollection.getActions().getComponents().forEach(actionProvider -> {
				ActionRegistry.registerActionHandler(actionProvider.getActionHandler());
			});

			// Set Default
			// TODO Set Default
			registryCollection.getAudioHandlers().setDefaultID(JavaFXHandlerFactory.class);
		} catch (Exception e) {
			Logger.error(e);
		}

		// Volume Management
		VolumeManager volumeManager = VolumeManager.getInstance();
		volumeManager.addFilter(new GlobalVolume());
		volumeManager.addFilter(new PadVolume());

		// Mapper
		BaseMapperListViewController.setInstance(new MapperListViewControllerImpl());
	}

	public Application.Parameters getParameters() {
		return parameters;
	}

	private void configureServer(SessionDelegate delegate) {
		// Load Server session key
		session = Session.load();

		if (session == null) {
			session = delegate.getSession();
		}

		if (session != Session.EMPTY) {
			// Connect to Server
			Server server = PlayPadPlugin.getServerHandler().getServer();
			try {
				server.connect(session.getKey());
			} catch (IOException | WebSocketException e) {
				Logger.error(e);
			} catch (SessionNotExisitsException ignored) {

			}
		}
	}

	public Session getSession() {
		return session;
	}

	public ModernDesign getModernDesign() {
		return modernDesign;
	}

	@Override
	public UpdateService getUpdateService() {
		return updateService;
	}
}
