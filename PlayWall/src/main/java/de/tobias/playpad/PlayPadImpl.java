package de.tobias.playpad;

import de.thecodelabs.logger.LogLevel;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.io.FileUtils;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.SystemUtils;
import de.thecodelabs.versionizer.service.UpdateService;
import de.tobias.playpad.design.ModernDesignHandler;
import de.tobias.playpad.initialize.*;
import de.tobias.playpad.log.LogSeasons;
import de.tobias.playpad.plugin.*;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.ConnectionState;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.Session;
import de.tobias.playpad.server.SessionDelegate;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MainViewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PlayPadImpl implements PlayPad {

	private Application.Parameters parameters;

	private List<WindowListener<IMainViewController>> mainViewListeners = new ArrayList<>();
	private List<SettingsListener> settingsListeners = new ArrayList<>();
	private List<PadListener> padListeners = new ArrayList<>();

	private MainViewController mainViewController;
	private Image stageIcon;
	private Project currentProject;

	private Module module;

	private UpdateService updateService;
	protected GlobalSettings globalSettings;
	private ModernDesignHandler modernDesign;

	private Session session;

	PlayPadImpl(Application.Parameters parameters) {
		this.parameters = parameters;

		App app = ApplicationUtils.getApplication();
		this.module = new Module(app.getInfo().getName(), app.getInfo().getIdentifier());
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
	public GlobalSettings getGlobalSettings() {
		return globalSettings;
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
			Logger.error(e);
		}

		ModernPluginManager.getInstance().showdown();
		Worker.shutdown();
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
				Platform.setImplicitExit(true);
			});
		} else {
			currentProject = project;
			mainViewController.openProject(project);
		}
	}

	public void startup(SessionDelegate delegate, PlayPadInitializer.Listener listener) {
		PlayPadInitializer initializer = new PlayPadInitializer(this, listener);

		initializer.submit(new LocalizationLoadingTask());
		initializer.submit(new GlobalSettingsLoadingTask());
		initializer.submit(new KeyboardMappingLoadingTask());

		initializer.submit(new ServiceInitializationTask());

		initializer.submit(new VersionizerSetupTask());
		initializer.submit(new ComponentLoadingTask());
		initializer.submit(new MidiActionsInitializerTask());
		initializer.submit(new VolumeInitializerTask());

		initializer.submit(new ProfileLoadingTask());

		initializer.submit(new ServerInitializeTask(delegate));

		initializer.submit(new NativeAppInitializerTask());
		initializer.submit(new PluginLoadingTask());
		initializer.submit(new ProjectsLoadingTask());

		initializer.submit(new CheckUpdateTask());

		initializer.submit(new OpenLastDocumentTask()); // abort if project is opened
		initializer.submit(new ProjectParameterOpenTask()); // abort if project is opened

		initializer.start();
	}

	public Application.Parameters getParameters() {
		return parameters;
	}

	public ModernDesignHandler getModernDesign() {
		return modernDesign;
	}

	public void setModernDesign(ModernDesignHandler modernDesign) {
		this.modernDesign = modernDesign;
	}

	@Override
	public UpdateService getUpdateService() {
		return updateService;
	}

	/*
	Getter / Setter
	 */

	@Override
	public Project getCurrentProject() {
		return currentProject;
	}

	@Override
	public Image getIcon() {
		return stageIcon;
	}

	public void setIcon(Image icon) {
		this.stageIcon = icon;
	}

	public Module getModule() {
		return module;
	}

	public void setGlobalSettings(GlobalSettings globalSettings) {
		this.globalSettings = globalSettings;
	}

	public void setUpdateService(UpdateService updateService) {
		this.updateService = updateService;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
}
