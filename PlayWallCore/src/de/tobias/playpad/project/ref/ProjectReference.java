package de.tobias.playpad.project.ref;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.plugin.ModernPluginManager;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.Project;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProjectReference implements Displayable {

	private UUID uuid;
	private String name;

	private ProfileReference profileReference;
	private Set<Module> requestedModules;

	private long lastModified;

	public ProjectReference(UUID uuid, String name, ProfileReference profileReference) {
		this.uuid = uuid;
		this.name = name;
		this.lastModified = System.currentTimeMillis();
		this.profileReference = profileReference;
		requestedModules = new HashSet<>();

		updateDisplayProperty();
	}

	public ProjectReference(UUID uuid, String name, ProfileReference profileReference, Set<Module> modules) {
		this.uuid = uuid;
		this.name = name;
		this.lastModified = System.currentTimeMillis();
		this.profileReference = profileReference;
		requestedModules = modules;

		updateDisplayProperty();
	}

	public ProjectReference(UUID uuid, String name, long lastModified, ProfileReference profileReference) {
		this.uuid = uuid;
		this.name = name;
		this.lastModified = lastModified;
		this.profileReference = profileReference;
		requestedModules = new HashSet<>();

		updateDisplayProperty();
	}

	public ProjectReference(UUID uuid, String name, long lastModified, ProfileReference profileReference, Set<Module> requestedModules) {
		this.uuid = uuid;
		this.name = name;
		this.lastModified = lastModified;
		this.profileReference = profileReference;
		this.requestedModules = requestedModules;

		updateDisplayProperty();
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setName(String name) {
		this.name = name;
		updateDisplayProperty();
	}

	public Set<Module> getRequestedModules() {
		return requestedModules;
	}

	public void addRequestedModule(Module module) {
		requestedModules.add(module);
	}

	public Set<Module> getMissedModules() {
		Set<Module> missedModules = new HashSet<>();
		Collection<Module> activeModules = ModernPluginManager.getInstance().getModules();
		for (Module requested : requestedModules) {
			if (!activeModules.contains(requested)) {
				missedModules.add(requested);
			}
		}

		if (profileReference != null) {
			for (Module requested : profileReference.getRequestedModules()) {
				if (!activeModules.contains(requested)) {
					missedModules.add(requested);
				}
			}
		}
		return missedModules;
	}

	public ProfileReference getProfileReference() {
		return profileReference;
	}

	public void setProfileReference(ProfileReference profileReference) {
		this.profileReference = profileReference;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getFileName() {
		return uuid + Project.FILE_EXTENSION;
	}

	public Path getProjectPath() {
		App application = ApplicationUtils.getApplication();
		Path projectPath = application.getPath(PathType.DOCUMENTS, getFileName());
		return projectPath;
	}

	private StringProperty displayProperty = new SimpleStringProperty(toString());

	@Override
	public StringProperty displayProperty() {
		return displayProperty;
	}

	private void updateDisplayProperty() {
		displayProperty.set(toString());
	}
}
