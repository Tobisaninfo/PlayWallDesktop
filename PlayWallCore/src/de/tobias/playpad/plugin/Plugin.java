package de.tobias.playpad.plugin;

public class Plugin {

	private String name;
	private String fileName;
	private String url;
	private boolean active;

	public Plugin(String name, String fileName, String url, boolean active) {
		this.name = name;
		this.fileName = fileName;
		this.url = url;
		this.active = active;
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

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Plugin) {
			Plugin p2 = (Plugin) obj;
			return p2.active == active && p2.fileName.equals(fileName) && p2.name.equals(name) && p2.url.equals(url);
		} else {
			return super.equals(obj);
		}
	}
}
