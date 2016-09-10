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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import org.dom4j.DocumentException;

import de.tobias.playpad.action.mapper.MapperRegistry;
import de.tobias.playpad.audio.JavaFXAudioHandler;
import de.tobias.playpad.design.modern.ModernGlobalDesign;
import de.tobias.playpad.midi.device.DeviceRegistry;
import de.tobias.playpad.midi.device.PD12;
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
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.FileUtils;
import de.tobias.utils.util.SystemUtils;
import de.tobias.utils.util.Worker;
import javafx.scene.image.Image;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;

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

	protected GlobalSettings globalSettings;

	public PlayPadImpl(GlobalSettings globalSettings) {
		pluginManager = PluginManagerFactory.createPluginManager();
		deletedPlugins = new HashSet<>();

		this.globalSettings = globalSettings;
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

	public void loadPlugin(URI uri) {
		pluginManager.addPluginsFrom(uri);
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

			registryCollection.getActions().loadComponentsFromFile("de/tobias/playpad/components/Actions.xml");
			registryCollection.getAudioHandlers().loadComponentsFromFile("de/tobias/playpad/components/AudioHandler.xml");
			registryCollection.getDragModes().loadComponentsFromFile("de/tobias/playpad/components/DragMode.xml");
			registryCollection.getDesigns().loadComponentsFromFile("de/tobias/playpad/components/Design.xml");
			registryCollection.getMappers().loadComponentsFromFile("de/tobias/playpad/components/Mapper.xml");
			registryCollection.getPadContents().loadComponentsFromFile("de/tobias/playpad/components/PadContent.xml");
			registryCollection.getTriggerItems().loadComponentsFromFile("de/tobias/playpad/components/Trigger.xml");
			registryCollection.getMainLayouts().loadComponentsFromFile("de/tobias/playpad/components/Layout.xml");

			// Set Default
			registryCollection.getAudioHandlers().setDefaultID(JavaFXAudioHandler.TYPE);
			registryCollection.getDesigns().setDefaultID(ModernGlobalDesign.TYPE);
		} catch (IllegalAccessException | ClassNotFoundException | InstantiationException | IOException | DocumentException
				| NoSuchComponentException e) {
			e.printStackTrace();
		}

		// Key Bindings
		GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();
		globalSettings.getKeyCollection().loadDefaultFromFile("de/tobias/playpad/components/Keys.xml", resourceBundle);

		// Mapper
		MapperRegistry.setOverviewViewController(new MapperOverviewViewController());
	}
}
