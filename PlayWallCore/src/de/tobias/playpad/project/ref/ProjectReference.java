package de.tobias.playpad.project.ref;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.plugin.ModernPluginManager;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.server.sync.conflict.Version;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import javafx.beans.property.*;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ProjectReference implements Displayable {

	private UUID uuid;
	private StringProperty nameProperty;

	private ProfileReference profileReference;
	private Set<Module> requestedModules;

	private BooleanProperty sync;
	private long lastModified;

	/**
	 * Create a project reference withput profile and with sync option
	 *
	 * @param uuid uuid
	 * @param name name
	 */
	public ProjectReference(UUID uuid, String name) {
		this(uuid, name, null, false);
		updateDisplayProperty();
	}

	public ProjectReference(UUID uuid, String name, ProfileReference profileReference, boolean sync) {
		this(uuid, name, System.currentTimeMillis(), profileReference, new HashSet<>(), sync);
		updateDisplayProperty();
	}

	public ProjectReference(UUID uuid, String name, ProfileReference profileReference, Set<Module> modules, boolean sync) {
		this(uuid, name, System.currentTimeMillis(), profileReference, modules, sync);
		updateDisplayProperty();
	}

	public ProjectReference(UUID uuid, String name, long lastModified, ProfileReference profileReference, boolean sync) {
		this(uuid, name, lastModified, profileReference, new HashSet<>(), sync);
		updateDisplayProperty();
	}

	public ProjectReference(UUID uuid, String name, long lastModified, ProfileReference profileReference, Set<Module> requestedModules, boolean sync) {
		this.uuid = uuid;
		this.nameProperty = new SimpleStringProperty(name);
		this.sync = new SimpleBooleanProperty(sync);

		this.lastModified = lastModified;
		this.profileReference = profileReference;

		this.requestedModules = requestedModules;

		updateDisplayProperty();
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getName() {
		return nameProperty.get();
	}

	public void setName(String name) {
		this.nameProperty.set(name);
		updateDisplayProperty();
	}

	public StringProperty nameProperty() {
		return nameProperty;
	}

	/**
	 * Get the sync property of the project reference. Set the sync property by using the {@link ProjectReferenceManager#setSync(ProjectReference, boolean)}
	 *
	 * @return sync
	 */
	public boolean isSync() {
		return sync.get();
	}

	void setSync(boolean sync) {
		this.sync.set(sync);
	}

	public ReadOnlyBooleanProperty syncProperty() {
		return sync;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public long getLastModified() {
		return lastModified;
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
		requestedModules.stream()
				.filter(i -> !activeModules.contains(i))
				.forEach(missedModules::add);
		if (profileReference != null) {
			profileReference.getRequestedModules().stream()
					.filter(i -> !activeModules.contains(i))
					.forEach(missedModules::add);
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
		return getName();
	}

	// File Path Handling
	public String getFileName() {
		return uuid + Project.FILE_EXTENSION;
	}

	public Path getProjectPath() {
		App application = ApplicationUtils.getApplication();
		return application.getPath(PathType.DOCUMENTS, getFileName());
	}

	// Display
	private StringProperty displayProperty = new SimpleStringProperty();

	@Override
	public StringProperty displayProperty() {
		return displayProperty;
	}

	private void updateDisplayProperty() {
		displayProperty.set(toString());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ProjectReference that = (ProjectReference) o;

		return uuid != null ? uuid.equals(that.uuid) : that.uuid == null;
	}

	public Version getVersion() {
		return new Version(lastModified, "Local", true); // TODO Localize
	}
}
