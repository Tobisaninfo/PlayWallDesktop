package de.tobias.playpad;

import com.neovisionaries.ws.client.WebSocketException;
import de.tobias.playpad.audio.JavaFXHandlerFactory;
import de.tobias.playpad.midi.device.DeviceRegistry;
import de.tobias.playpad.midi.device.PD12;
import de.tobias.playpad.plugin.*;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.Session;
import de.tobias.playpad.server.SessionDelegate;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.view.MapperOverviewViewController;
import de.tobias.playpad.viewcontroller.BaseMapperOverviewViewController;
import de.tobias.playpad.viewcontroller.IPadSettingsViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MainViewController;
import de.tobias.playpad.viewcontroller.option.IProfileSettingsViewController;
import de.tobias.playpad.volume.GlobalVolume;
import de.tobias.playpad.volume.PadVolume;
import de.tobias.playpad.volume.VolumeManager;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.util.FileUtils;
import de.tobias.utils.util.SystemUtils;
import de.tobias.utils.util.Worker;
import javafx.application.Application;
import javafx.scene.image.Image;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class PlayPadImpl implements PlayPad {

    private Application.Parameters parameters;

    protected List<WindowListener<IMainViewController>> mainViewListeners = new ArrayList<>();
    protected List<WindowListener<IProfileSettingsViewController>> settingsViewListeners = new ArrayList<>();
    protected List<WindowListener<IPadSettingsViewController>> padSettingsViewListeners = new ArrayList<>();
    protected List<SettingsListener> settingsListeners = new ArrayList<>();
    protected List<PadListener> padListeners = new ArrayList<>();

    private MainViewController mainViewController;
    private Project currentProject;
    private static Module module;

    protected GlobalSettings globalSettings;

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
        PlayPadPlugin.getRegistryCollection().getAudioHandlers().getComponents().forEach(i ->
        {
            if (i instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) i).close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        PlayPadPlugin.getServerHandler().getServer().disconnect();

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
        registerComponents(resourceBundle);
        configureServer(delegate);
    }

    private void registerComponents(ResourceBundle resourceBundle) {
        // Midi
        DeviceRegistry.getFactoryInstance().registerDevice(PD12.NAME, PD12.class);

        try {
            // Load Components
            RegistryCollection registryCollection = PlayPadPlugin.getRegistryCollection();

            registryCollection.getActions().loadComponentsFromFile("de/tobias/playpad/components/Actions.xml", module, resourceBundle);
            registryCollection.getAudioHandlers().loadComponentsFromFile("de/tobias/playpad/components/AudioHandler.xml", module, resourceBundle);
            registryCollection.getDragModes().loadComponentsFromFile("de/tobias/playpad/components/DragMode.xml", module, resourceBundle);
            registryCollection.getMappers().loadComponentsFromFile("de/tobias/playpad/components/Mapper.xml", module, resourceBundle);
            registryCollection.getPadContents().loadComponentsFromFile("de/tobias/playpad/components/PadContent.xml", module, resourceBundle);
            registryCollection.getTriggerItems().loadComponentsFromFile("de/tobias/playpad/components/Trigger.xml", module, resourceBundle);
            registryCollection.getMainLayouts().loadComponentsFromFile("de/tobias/playpad/components/Layout.xml", module, resourceBundle);

            // Set Default
            // TODO Set Default
            registryCollection.getAudioHandlers().setDefaultID(JavaFXHandlerFactory.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Volume Management
        VolumeManager volumeManager = VolumeManager.getInstance();
        volumeManager.addFilter(new GlobalVolume());
        volumeManager.addFilter(new PadVolume());

        // Mapper
        BaseMapperOverviewViewController.setInstance(new MapperOverviewViewController());
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

        // Connect to Server
        Server server = PlayPadPlugin.getServerHandler().getServer();
        try {
            server.connect(session.getKey());
        } catch (IOException | WebSocketException e) {
            e.printStackTrace();
        }
    }

    public Session getSession() {
        return session;
    }
}
