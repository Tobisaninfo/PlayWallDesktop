package de.tobias.playpad.profile.ref;

import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.tobias.playpad.Displayable;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.profile.Profile;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Container für Profile Referenzen
 *
 * @author tobias
 * @see Profile
 * @since 5.0.0
 */
public class ProfileReference implements Displayable {

	private final UUID uuid;
	private String name;
	private Set<Module> requestedModules;

	/**
	 * Erstellt eine neue Referenz mit einer Random UUID.
	 *
	 * @param name Name
	 */
	public ProfileReference(String name) {
		this(UUID.randomUUID(), name);
	}

	/**
	 * Erstellt eine neue Referenz mit Namen und UUID.
	 *
	 * @param uuid UUID
	 * @param name Name
	 */
	public ProfileReference(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
		requestedModules = new HashSet<>();
		updateDisplayProperty();
	}

	/**
	 * Erstellt eine neue Referenz mit Namen und UUID.
	 *
	 * @param uuid             UUID
	 * @param name             Name
	 * @param requestedModules List of requested modules
	 */
	public ProfileReference(UUID uuid, String name, Set<Module> requestedModules) {
		this(uuid, name);
		this.requestedModules = requestedModules;
	}

	/**
	 * Gibt den Namen zurück
	 *
	 * @return Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gibt die UUID zurück
	 *
	 * @return uudi
	 */
	public UUID getUuid() {
		return uuid;
	}

	/**
	 * Setzt einen neuen Namen.
	 *
	 * @param name Neuer Name
	 */
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

	/**
	 * Gibt einen Pfad für einen Dateinamen in diesem Profile zurück.
	 *
	 * @param name Name der Datei
	 * @return Path für die Datei
	 */
	public Path getCustomFilePath(String name) {
		return ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, getFileName(), name);
	}

	/**
	 * Gibt den internen (File-) Namen des Profiles zurück.
	 *
	 * @return Ordnernamen
	 */
	public String getFileName() {
		return uuid.toString();
	}

	@Override
	public String toString() {
		return name;
	}

	// Displayable
	private StringProperty displayProperty = new SimpleStringProperty(toString());

	@Override
	public StringProperty displayProperty() {
		return displayProperty;
	}

	private void updateDisplayProperty() {
		displayProperty.set(toString());
	}
}
