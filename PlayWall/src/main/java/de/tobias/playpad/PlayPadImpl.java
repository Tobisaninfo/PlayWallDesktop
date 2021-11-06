package de.tobias.playpad;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.io.FileUtils;
import de.thecodelabs.utils.io.IOUtils;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.SystemUtils;
import de.thecodelabs.versionizer.service.UpdateService;
import de.tobias.playpad.design.ModernDesignHandler;
import de.tobias.playpad.initialize.*;
import de.tobias.playpad.plugin.*;
import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ProjectReader;
import de.tobias.playpad.project.loader.ProjectLoader;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.ConnectionState;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.Session;
import de.tobias.playpad.server.SessionDelegate;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.viewcontroller.dialog.project.ProjectLoadDialog;
import de.tobias.playpad.viewcontroller.dialog.project.ProjectReaderDelegateImpl;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MainViewController;
import de.tobias.playpad.viewcontroller.option.GlobalSettingsTabViewController;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Window;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PlayPadImpl implements PlayPad {

	private final Application.Parameters parameters;

	private final List<MainWindowListener> mainViewListeners = new ArrayList<>();
	private final List<SettingsListener> settingsListeners = new ArrayList<>();
	private final List<PadListener> padListeners = new ArrayList<>();
	private final List<GlobalListener> globalListeners = new ArrayList<>();
	private final List<Supplier<ProfileSettingsTabViewController>> additionalProfileSettingsTabs = new ArrayList<>();
	private final List<Supplier<GlobalSettingsTabViewController>> additionalGlobalSettingsTabs = new ArrayList<>();

	private MainViewController mainViewController;
	private Image stageIcon;
	private byte[] stageIconData;

	private Project currentProject;

	private final Module module;

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
	public void addMainViewListener(MainWindowListener listener) {
		mainViewListeners.add(listener);
	}

	public List<MainWindowListener> getMainViewListeners() {
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
	public void addGlobalListener(GlobalListener globalListener) {
		globalListeners.add(globalListener);
	}

	@Override
	public void removeGlobalListener(GlobalListener globalListener) {
		globalListeners.remove(globalListener);
	}

	@Override
	public List<GlobalListener> getGlobalListeners() {
		return globalListeners;
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
			Path applicationSupportPath = SystemUtils.getApplicationSupportDirectoryPath("de.tobias.playpad.PlayPadMain");
			FileUtils.deleteDirectory(applicationSupportPath);
		} catch (IOException e) {
			Logger.error(e);
		}

		ModernPluginManager.getInstance().showdown();
		Worker.shutdown();
	}

	@Override
	public void openProject(ProjectReference projectReference, Consumer<NVC> onLoaded) throws ProjectNotFoundException, ProjectReader.ProjectReaderDelegate.ProfileAbortException, ProfileNotFoundException, DocumentException, IOException {
		if (mainViewController != null) {
			mainViewController.closeProject();
			globalListeners.forEach(l -> l.projectClosed(currentProject));
		}

		Window owner = mainViewController != null ? mainViewController.getContainingWindow() : null;

		ProjectLoader loader = new ProjectLoader(projectReference);
		loader.setDelegate(ProjectReaderDelegateImpl.getInstance(owner));
		loader.setListener(new ProjectLoadDialog());

		currentProject = loader.load();

		if (mainViewController == null) {
			mainViewController = new MainViewController(e -> {
				mainViewController.openProject(currentProject);
				mainViewListeners.forEach(l -> l.onInit(mainViewController));

				if (onLoaded != null) {
					onLoaded.accept(e);
				}

				globalListeners.forEach(l -> l.projectOpened(currentProject));
				Platform.setImplicitExit(true);
			});
		} else {
			mainViewController.openProject(currentProject);
			globalListeners.forEach(l -> l.projectOpened(currentProject));
		}
	}

	public void startup(SessionDelegate delegate, PlayPadInitializer.Listener listener) {
		PlayPadInitializer initializer = new PlayPadInitializer(this, listener);

		initializer.submit(new LocalizationLoadingTask());
		initializer.submit(new GlobalSettingsLoadingTask());
		initializer.submit(new KeyboardDefaultMappingTask());

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
		initializer.submit(new KeyboardLoadingMappingTask());

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

	void setModernDesign(ModernDesignHandler modernDesign) {
		this.modernDesign = modernDesign;
	}

	@Override
	public UpdateService getUpdateService() {
		return updateService;
	}

	@Override
	public void addAdditionalProfileSettingsTab(Supplier<ProfileSettingsTabViewController> tab) {
		additionalProfileSettingsTabs.add(tab);
	}

	@Override
	public List<Supplier<ProfileSettingsTabViewController>> getAdditionalProfileSettingsTabs() {
		return additionalProfileSettingsTabs;
	}

	@Override
	public void addGlobalSettingsTab(Supplier<GlobalSettingsTabViewController> tab) {
		additionalGlobalSettingsTabs.add(tab);
	}

	@Override
	public List<Supplier<GlobalSettingsTabViewController>> getGlobalSettingsTabs() {
		return additionalGlobalSettingsTabs;
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

	@Override
	public byte[] getIconData() {
		return stageIconData;
	}

	public void setIcon(Image icon, InputStream iconResource) {
		this.stageIcon = icon;
		try {
			stageIconData = IOUtils.inputStreamToByteArray(iconResource);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
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
