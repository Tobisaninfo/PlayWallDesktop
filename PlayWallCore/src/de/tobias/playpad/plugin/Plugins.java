package de.tobias.playpad.plugin;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;

public class Plugins {

	private static List<PluginDescription> availablePlugins;

	static {
		availablePlugins = new ArrayList<>();
	}

	public static List<PluginDescription> loadDescriptionFromServer(String pluginInfoURL, boolean fetch) throws IOException {
		if (availablePlugins.isEmpty() || fetch) {
			availablePlugins.clear();
			URL url = new URL(pluginInfoURL);

			FileConfiguration cfg = YamlConfiguration.loadConfiguration(url.openStream());

			// Iterate over all plugins that are online avialable
			for (String key : cfg.getConfigurationSection("plugins").getKeys(false)) {
				String id = cfg.getString("plugins." + key + ".id");
				String name = new String(cfg.getString("plugins." + key + ".name").getBytes(), "UTF-8");
				String pluginUrl = cfg.getString("plugins." + key + ".url");
				String fileName = cfg.getString("plugins." + key + ".filename");
				String version = cfg.getString("plugins." + key + ".version");
				long build = cfg.getLong("plugins." + key + ".build");

				List<String> dependencies = cfg.getStringList("plugins." + key + ".dependencies");

				boolean active = false;

				Path path = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, fileName);
				if (Files.exists(path))
					active = true;

				PluginDescription plugin = new PluginDescription(id, name, fileName, pluginUrl, version, build, active, dependencies);
				availablePlugins.add(plugin);
			}
		}
		return availablePlugins;
	}

	public static List<PluginDescription> getAvailablePlugins() {
		return availablePlugins;
	}

	public static void loadDependencies(PluginDescription plugin) {
		List<PluginDescription> dependencies = findDependencies(plugin);
		dependencies.forEach(p ->
		{
			Path decPath = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, p.getFileName());
			downloadPlugin(p, decPath);

			// Add Plugin to classpath
			PlayPadPlugin.getImplementation().loadPlugin(decPath.toUri());
		});
	}

	private static List<PluginDescription> findDependencies(PluginDescription plugin) {
		List<PluginDescription> plugins = new ArrayList<>();
		for (String dependencyName : plugin.getDependencies()) {
			for (PluginDescription desc : Plugins.getAvailablePlugins()) {
				if (desc.getName().equals(dependencyName)) {
					plugins.add(desc);
				}
			}
		}
		return plugins;
	}

	public static void downloadPlugin(PluginDescription plugin, Path path) {
		if (Files.notExists(path)) {
			try {
				Files.createDirectories(path.getParent());
				Files.copy(new URL(plugin.getUrl()).openStream(), path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
