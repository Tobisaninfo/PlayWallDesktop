package de.tobias.playpad.plugin;

import de.tobias.updater.client.Updatable;
import de.tobias.updater.client.UpdateRegistery;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import net.xeoh.plugins.base.Plugin;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by tobias on 10.02.17.
 */
public class ModernPluginManager {

	private static final String PLUGIN_INFO_TXT = "pluginInfo.txt";

	private PluginManager pluginManager;
	private Set<Path> deletedPlugins;
	private Set<Module> modules;

	private static ModernPluginManager instance;

	public static ModernPluginManager getInstance() {
		if (instance == null) {
			instance = new ModernPluginManager();
		}
		return instance;
	}

	private ModernPluginManager() {
		this.pluginManager = PluginManagerFactory.createPluginManager();
		this.deletedPlugins = new HashSet<>();
		this.modules = new HashSet<>();
	}

	public void addModule(Module module) {
		modules.add(module);
	}

	public void loadPlugin(ModernPlugin plugin) throws IOException {
		Path path = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, plugin.getFileName());
		if (Files.notExists(path)) {
			throw new IOException("File not found: " + path);
		}
		loadFile(path);
	}

	public void loadFile(Path path) {
		URI uri = path.toUri();

		pluginManager.addPluginsFrom(uri);

		// Registriert Funktionen aus Plugin (Module und Update, ...)
		PluginManagerUtil util = new PluginManagerUtil(pluginManager);
		Collection<Plugin> plugins = util.getPlugins();
		for (Plugin p : plugins) {
			if (p instanceof AdvancedPlugin) {
				AdvancedPlugin advancedPlugin = (AdvancedPlugin) p;
				Module module = advancedPlugin.getModule();
				Updatable updatable = advancedPlugin.getUpdatable();

				modules.add(module);
				UpdateRegistery.registerUpdateable(updatable);
			}
		}
	}

	public void unloadPlugin(ModernPlugin plugin) {
		Path path = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, plugin.getFileName());
		addDeletedPlugin(path);
	}

	/**
	 * Fügt ein Plugin hinzu, sich zu löschen.
	 *
	 * @param path Pfad zu einem Plugin
	 */
	private void addDeletedPlugin(Path path) {
		deletedPlugins.add(path);
	}

	public void deletePlugins() throws IOException {
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

	public void showdown() {
		// Delete Plugins Info Textfile --> Löschen dann beim Start.
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

		pluginManager.shutdown();
	}

	public boolean isActive(ModernPlugin plugin) {
		Path path = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, plugin.getFileName());
		return Files.exists(path) && !deletedPlugins.contains(path);
	}

	public Set<Module> getModules() {
		return modules;
	}
}