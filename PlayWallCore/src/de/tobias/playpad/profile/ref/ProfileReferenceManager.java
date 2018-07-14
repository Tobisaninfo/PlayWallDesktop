package de.tobias.playpad.profile.ref;

import de.tobias.playpad.profile.Profile;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.FileUtils;
import de.tobias.utils.util.FileUtils.FileActionAdapter;
import de.tobias.utils.xml.XMLHandler;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class ProfileReferenceManager {

	private ProfileReferenceManager() {}

	/**
	 * Liste mit allen Referenzen
	 */
	private static ProfileReferenceList profiles = new ProfileReferenceList();

	/**
	 * Sucht eine Referenz zu einer UUID raus.
	 * 
	 * @param profile
	 *            UUID des Profiles
	 * @return ProfileReferenz für die UUID
	 */
	public static ProfileReference getReference(UUID profile) {
		if (profile == null) {
			return null;
		}

		for (ProfileReference ref : profiles) {
			if (ref.getUuid().equals(profile)) {
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
	public static ProfileReferenceList getProfiles() {
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
	public static Profile newProfile(String name) throws IOException {
		ProfileReference ref = new ProfileReference(UUID.randomUUID(), name);
		ProfileReferenceManager.addProfile(ref);

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
	private static final String DEFAULT_PROFILE_NAME = "Default";

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
		ProfileReferenceManager.profiles.clear();

		Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, FILE_NAME);

		if (Files.exists(path)) {
			// Load data from xml
			XMLHandler<ProfileReference> handler = new XMLHandler<>(path);
			ProfileReferenceManager.profiles.setAll(handler.loadElements(PROFILE_ELEMENT, new ProfileReferenceSerializer()));
			System.out.println("Find Profile: " + ProfileReferenceManager.profiles);
		}

		// Add Default Element if list is empty
		if (ProfileReferenceManager.profiles.isEmpty()) {
			Profile profile = ProfileReferenceManager.newProfile(DEFAULT_PROFILE_NAME);
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
	public static void saveProfiles() throws IOException {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement(ROOT_ELEMENT);

		// Save data to xml
		XMLHandler<ProfileReference> handler = new XMLHandler<>(root);
		handler.saveElements(PROFILE_ELEMENT, ProfileReferenceManager.profiles, new ProfileReferenceSerializer());

		Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, FILE_NAME);
		if (Files.notExists(path)) {
			Files.createDirectories(path.getParent());
			Files.createFile(path);
		}
		XMLHandler.save(path, document);
	}

	public static boolean validateName(String name) {
		for (ProfileReference ref : profiles) {
			if (ref.getName().equals(name)) {
				return false;
			}
		}
		return true;
	}
}
