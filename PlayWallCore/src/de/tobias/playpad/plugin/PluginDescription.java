package de.tobias.playpad.plugin;

import java.util.List;

public class PluginDescription implements Comparable<PluginDescription> {

	private String id;
	private String name;
	private String fileName;
	private String url;

	private String version;
	private long build;

	private boolean active;
	private List<String> dependencies;

	public PluginDescription(String id, String name, String fileName, String url, String version, long build, boolean active,
			List<String> dependencies) {
		this.id = id;
		this.name = name;
		this.fileName = fileName;
		this.url = url;
		this.version = version;
		this.build = build;
		this.active = active;
		this.dependencies = dependencies;
	}

	public String getId() {
		return id;
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
		if (obj instanceof PluginDescription) {
			PluginDescription p2 = (PluginDescription) obj;
			return p2.active == active && p2.fileName.equals(fileName) && p2.id.equals(id) && p2.url.equals(url);
		} else {
			return super.equals(obj);
		}
	}

	@Override
	public int compareTo(PluginDescription o) {
		return getName().compareTo(o.getName());
	}

	@Override
	public String toString() {
		return name + " " + version + " (" + build + ")";
	}
}
