package de.tobias.playpad.plugin;

/**
 * Created by tobias on 08.02.17.
 */
public class ModernPlugin {

	private static final String EXTENSION = ".jar";

	private int id;
	private String name;
	private String displayName;
	private String description;

	private String version;
	private int build;

	public ModernPlugin() {
	}

	public ModernPlugin(int id, String name, String displayName, String description, String version, int build) {
		this.id = id;
		this.name = name;
		this.displayName = displayName;
		this.description = description;
		this.version = version;
		this.build = build;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	public String getVersion() {
		return version;
	}

	public int getBuild() {
		return build;
	}

	public String getFileName() {
		return getName() + EXTENSION;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
