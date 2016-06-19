package de.tobias.playpad.plugin;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;

public class Plugin implements Comparable<Plugin> {

	private String name;
	private String fileName;
	private String url;
	private boolean active;
	private List<String> dependencies;

	private static List<Plugin> plugins;

	static {
		plugins = new ArrayList<>();
	}

	public Plugin(String name, String fileName, String url, boolean active, List<String> dependencies) {
		this.name = name;
		this.fileName = fileName;
		this.url = url;
		this.active = active;
		this.dependencies = dependencies;
	}

	public String getName() {
		return name;
	}

	public String getFileName() {
		return fileName;
	}

	public String getUrl() {
		return url;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void addDependency(String id) {
		dependencies.add(id);
	}

	public List<String> getDependencies() {
		return dependencies;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Plugin) {
			Plugin p2 = (Plugin) obj;
			return p2.active == active && p2.fileName.equals(fileName) && p2.name.equals(name) && p2.url.equals(url);
		} else {
			return super.equals(obj);
		}
	}

	@Override
	public int compareTo(Plugin o) {
		return getName().compareTo(o.getName());
	}

	public static void load(String pluginInfoURL) throws IOException {
		plugins.clear();
		URL url = new URL(pluginInfoURL);

		FileConfiguration cfg = YamlConfiguration.loadConfiguration(url.openStream());

		// Iterate over all plugins that are online avialable
		for (String key : cfg.getConfigurationSection("plugins").getKeys(false)) {
			String name = new String(cfg.getString("plugins." + key + ".name").getBytes(), "UTF-8");
			String pluginUrl = cfg.getString("plugins." + key + ".url");
			String fileName = cfg.getString("plugins." + key + ".filename");

			List<String> dependencies = cfg.getStringList("plugins." + key + ".dependencies");

			boolean active = false;

			Path path = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, fileName);
			if (Files.exists(path))
				active = true;

			Plugin plugin = new Plugin(name, fileName, pluginUrl, active, dependencies);
			plugins.add(plugin);
		}
	}

	public static List<Plugin> getPlugins() {
		return plugins;
	}
}
