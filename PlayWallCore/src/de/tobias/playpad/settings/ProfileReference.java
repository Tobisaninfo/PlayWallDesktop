package de.tobias.playpad.settings;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.plugin.Module;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.FileUtils;
import de.tobias.utils.util.FileUtils.FileActionAdapter;
import de.tobias.utils.xml.XMLHandler;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Container für Profile Referenzen
 * 
 * @author tobias
 * 
 * @see Profile
 * @since 5.0.0
 */
public class ProfileReference implements Displayable {

	private static final String DEFAULT_PROFILE_NAME = "Default";

	private final UUID uuid;
	private String name;
	private Set<Module> requestedModules;

	/**
	 * Erstellt eine neue Referenz mit einer Random UUID.
	 * 
	 * @param name
	 *            Name
	 */
	public ProfileReference(String name) {
		this.name = name;
		this.uuid = UUID.randomUUID();
		requestedModules = new HashSet<>();
		updateDisplayProperty();
	}

	/**
	 * Erstellt eine neue Referenz mit Namen und UUID.
	 * 
	 * @param uuid
	 *            UUID
	 * @param name
	 *            Name
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
	 * @param uuid
	 *            UUID
	 * @param name
	 *            Name
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
	 * @param name
	 *            Neuer Name
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

	// Verwaltungsmethoden für Profile Referenzen // TODO Extract in Extra Class

	/**
	 * Liste mit allen Referenzen
	 */
	private static List<ProfileReference> profiles = new ProfileReferenceList();

	/**
	 * Sucht eine Referenz zu einer UUID raus.
	 * 
	 * @param profile
	 *            UUID des Profiles
	 * @return ProfileReferenz für die UUID
	 */
	public static ProfileReference getReference(UUID profile) {
		for (ProfileReference ref : profiles) {
			if (ref.uuid.equals(profile)) {
				return ref;
			}
		}
		return null;
	}

	/**
	 * Listet alle verfügbaren Profil Refernzen auf.
	 * 
	 * @return Liste von Referenzen (Name, UUID)
	 */
	public static List<ProfileReference> getProfiles() {
		return profiles;
	}

	/**
	 * Create and Save a new Profile
	 * 
	 * @param name
	 *            Profile Name
	 * @return Referenz auf das neue Profile.
	 * @throws UnsupportedEncodingException
	 *             Fehler beim Speichern des XML
	 * @throws IOException
	 *             IO Fehler
	 */
	public static Profile newProfile(String name) throws UnsupportedEncodingException, IOException {
		ProfileReference ref = new ProfileReference(UUID.randomUUID(), name);
		ProfileReference.addProfile(ref);

		Profile profile = new Profile(ref);
		profile.save();

		return profile;
	}

	/**
	 * Fügt ein Profile hinzu und erstellt den Ordner auf der Festplatte.
	 * 
	 * @param ref
	 *            Referenz zu diesem Profile (Name, UUID)
	 */
	public static void addProfile(ProfileReference ref) {
		// MODEL
		profiles.add(ref);

		// DRIVE
		Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, ref.getFileName());
		if (Files.notExists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Dupliziert eine Profile. Dabei werden die Dateien auf der Festplatte auch dupliziert.
	 * 
	 * @param src
	 *            Name des Orginalprofiles
	 * @param des
	 *            Name des neuen Profiles
	 * @throws IOException
	 *             IO Fehler
	 */
	public static void duplicate(ProfileReference src, ProfileReference des) throws IOException {
		if (!des.equals(src)) {
			FileUtils.loopThroughDirectory(ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, src.getFileName()),
					new FileActionAdapter() {

						@Override
						public void onFile(Path file) throws IOException {
							String name = file.getFileName().toString();
							Path desPath = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, des.getFileName(), name);
							if (Files.notExists(desPath.getParent())) {
								Files.createDirectories(desPath.getParent());
							}
							Files.copy(file, desPath, StandardCopyOption.REPLACE_EXISTING);
						}
					});

			profiles.add(des);
		}
	}

	/**
	 * Entfernt eine ProfileReferenz und das Profile.
	 * 
	 * @param ref
	 *            Profile Referenz
	 * @throws IOException
	 *             IO Fehler
	 */
	public static void removeProfile(ProfileReference ref) throws IOException {
		// Model
		profiles.remove(ref);

		// DRIVE
		Path root = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, ref.getFileName());
		if (Files.exists(root)) {
			Files.walk(root).forEach(path ->
			{
				try {
					if (!Files.isDirectory(path))
						Files.delete(path);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			Files.delete(root);
		}
	}

	// Load and Save

	private static final String FILE_NAME = "Profiles.xml";
	private static final String ROOT_ELEMENT = "Settings";
	private static final String PROFILE_ELEMENT = "Profile";

	/**
	 * Lädt alle Profile Referenzen.
	 * 
	 * @throws IOException
	 *             IO Fehler
	 * @throws DocumentException
	 *             XML Fehler
	 */
	public static void loadProfiles() throws IOException, DocumentException {
		profiles.clear();

		Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, FILE_NAME);

		if (Files.exists(path)) {
			// Load data from xml
			XMLHandler<ProfileReference> handler = new XMLHandler<>(path);
			profiles = handler.loadElements(PROFILE_ELEMENT, new ProfileReferenceSerializer());
			System.out.println(profiles);
		}

		// Add Default Element if list is empty
		if (profiles.isEmpty()) {
			Profile profile = newProfile(DEFAULT_PROFILE_NAME);
			profile.save();
		}
	}

	/**
	 * Speichert alle Profile Referenzen in eine Datei.
	 * 
	 * @throws UnsupportedEncodingException
	 *             XML Fehler
	 * @throws IOException
	 *             IO Fehler
	 */
	public static void saveProfiles() throws UnsupportedEncodingException, IOException {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement(ROOT_ELEMENT);

		// Save data to xml
		XMLHandler<ProfileReference> handler = new XMLHandler<>(root);
		handler.saveElements(PROFILE_ELEMENT, profiles, new ProfileReferenceSerializer());

		Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, FILE_NAME);
		if (Files.notExists(path)) {
			Files.createDirectories(path.getParent());
			Files.createFile(path);
		}
		XMLHandler.save(path, document);
	}

	/**
	 * Gibt einen Pfad für einen Dateinamen in diesem Profile zurück.
	 * 
	 * @param name
	 *            Name der Datei
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
