package de.tobias.playpad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import de.tobias.playpad.audio.JavaFXHandlerFactory;
import de.tobias.playpad.design.modern.ModernDesignFactory;
import org.dom4j.DocumentException;

import de.tobias.playpad.action.mapper.MapperRegistry;
import de.tobias.playpad.audio.JavaFXAudioHandler;
import de.tobias.playpad.design.modern.ModernGlobalDesign;
import de.tobias.playpad.midi.device.DeviceRegistry;
import de.tobias.playpad.midi.device.PD12;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.plugin.AdvancedPlugin;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.PadListener;
import de.tobias.playpad.plugin.SettingsListener;
import de.tobias.playpad.plugin.WindowListener;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.view.MapperOverviewViewController;
import de.tobias.playpad.viewcontroller.IPadSettingsViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MainViewController;
import de.tobias.playpad.viewcontroller.option.IProfileSettingsViewController;
import de.tobias.playpad.volume.GlobalVolume;
import de.tobias.playpad.volume.PadVolume;
import de.tobias.updater.client.Updatable;
import de.tobias.updater.client.UpdateRegistery;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.FileUtils;
import de.tobias.utils.util.SystemUtils;
import de.tobias.utils.util.Worker;
import javafx.scene.image.Image;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;

public class PlayPadImpl implements PlayPad {

	private static final String PLUGIN_INFO_TXT = "pluginInfo.txt";

	protected List<WindowListener<IMainViewController>> mainViewListeners = new ArrayList<>();
	protected List<WindowListener<IProfileSettingsViewController>> settingsViewListeners = new ArrayList<>();
	protected List<WindowListener<IPadSettingsViewController>> padSettingsViewListeners = new ArrayList<>();
	protected List<SettingsListener> settingsListeners = new ArrayList<>();
	protected List<PadListener> padListeners = new ArrayList<>();

	private PluginManager pluginManager;
	private Set<Path> deletedPlugins;

	private MainViewController mainViewController;
	private Project currentProject;
	private static Module module;

	protected GlobalSettings globalSettings;

	private Set<Module> modules;

	public PlayPadImpl(GlobalSettings globalSettings) {
		App app = ApplicationUtils.getApplication();
		module = new Module(app.getInfo().getName(), app.getInfo().getIdentifier());

		pluginManager = PluginManagerFactory.createPluginManager();
		deletedPlugins = new HashSet<>();
		modules = new HashSet<>();

		this.globalSettings = globalSettings;

		getModules().add(module); // Add Main Module
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

	/**
	 * Fügt ein Plugin hinzu, sich zu löschen.
	 * 
	 * @param path
	 *            Pfad zu einem Plugin
	 */
	public void addDeletedPlugin(Path path) {
		deletedPlugins.add(path);
	}

	/**
	 * Gibt alle Plugins zurück, die gelöscht werden sollen.
	 * 
	 * @return Plugins
	 */
	public Set<Path> getDeletedPlugins() {
		return deletedPlugins;
	}

	void deletePlugins() throws IOException {
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		// Delete Plugins Info Textfile --> Löschen dann beim Start.
		Path pluginInfoPath = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, PLUGIN_INFO_TXT);
		try {
			if (Files.notExists(pluginInfoPath)) {
				Files.createDirectories(pluginInfoPath.getParent());
				Files.createFile(pluginInfoPath);
			}
			PrintWriter deleteWriter = new PrintWriter(Files.newOutputStream(pluginInfoPath));
			for (Path path : getDeletedPlugins()) {
				deleteWriter.println(path.toString());
			}
			deleteWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			FileUtils.deleteDirectory(SystemUtils.getApplicationSupportDirectoryPath("de.tobias.playpad.PlayPadMain"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		pluginManager.shutdown();
		Worker.shutdown();
	}

	@Override
	public void loadPlugin(URI uri) {
		pluginManager.addPluginsFrom(uri);

		// Registriert Funktionen aus Plugin (Module und Update, ...)
		PluginManagerUtil util = new PluginManagerUtil(pluginManager);
		Collection<Plugin> plugins = util.getPlugins();
		for (Plugin plugin : plugins) {
			if (plugin instanceof AdvancedPlugin) {
				AdvancedPlugin advancedPlugin = (AdvancedPlugin) plugin;
				Module module = advancedPlugin.getModule();
				Updatable updatable = advancedPlugin.getUpdatable();

				modules.add(module);
				UpdateRegistery.registerUpdateable(updatable);
			}
		}
	}

	@Override
	public GlobalSettings getGlobalSettings() {
		return globalSettings;
	}

	public void openProject(Project project) {
		if (mainViewController == null) {
			mainViewController = new MainViewController(mainViewListeners);
		}
		currentProject = project;
		mainViewController.openProject(project);
	}

	public Project getCurrentProject() {
		return currentProject;
	}

	public void startup(ResourceBundle resourceBundle) {
		registerComponents(resourceBundle);
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
			registryCollection.getDesigns().loadComponentsFromFile("de/tobias/playpad/components/Design.xml", module, resourceBundle);
			registryCollection.getMappers().loadComponentsFromFile("de/tobias/playpad/components/Mapper.xml", module, resourceBundle);
			registryCollection.getPadContents().loadComponentsFromFile("de/tobias/playpad/components/PadContent.xml", module, resourceBundle);
			registryCollection.getTriggerItems().loadComponentsFromFile("de/tobias/playpad/components/Trigger.xml", module, resourceBundle);
			registryCollection.getMainLayouts().loadComponentsFromFile("de/tobias/playpad/components/Layout.xml", module, resourceBundle);

			// Set Default
			// TODO Set Default
			registryCollection.getAudioHandlers().setDefaultID(JavaFXHandlerFactory.class);
			registryCollection.getDesigns().setDefaultID(ModernDesignFactory.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Volume Management
		Pad.getVolumeManager().addFilter(new GlobalVolume());
		Pad.getVolumeManager().addFilter(new PadVolume());

		// Mapper
		MapperRegistry.setOverviewViewController(new MapperOverviewViewController());

	}

	@Override
	public Set<Module> getModules() {
		return modules;
	}
}
