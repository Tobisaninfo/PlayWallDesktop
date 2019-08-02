package de.tobias.playpad.plugin;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.plugins.Plugin;
import de.thecodelabs.plugins.PluginArtifact;
import de.thecodelabs.plugins.PluginManager;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.tobias.playpad.PlayPadPlugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
		this.pluginManager = PluginManager.getInstance();
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
		loadFile(path.getParent());
	}

	public void loadFile(Path path) {
		if (path.endsWith("classes")) {
			pluginManager.addFile(path);
		} else {
			pluginManager.addFolder(path);
		}
		pluginManager.loadPlugins();

		// Registriert Funktionen aus Plugin (Module)
		for (Plugin p : pluginManager.getPlugins()) {
			if (p instanceof PlayPadPluginStub) {
				modules.add(((PlayPadPluginStub) p).getModule());
			}

			if (p instanceof PluginArtifact) {
				PlayPadPlugin.getInstance().getUpdateService().addArtifact(((PluginArtifact) p).getArtifact(), path);
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
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(pluginInfoPath)))) {
				String line;
				while ((line = reader.readLine()) != null) {
					Path plugin = Paths.get(line);
					Files.deleteIfExists(plugin);
				}
			}
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
			Logger.error(e);
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
