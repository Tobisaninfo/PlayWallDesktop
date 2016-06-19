package de.tobias.playpad.settings;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import de.tobias.playpad.Displayable;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.list.UniqList;
import de.tobias.utils.util.FileUtils;
import de.tobias.utils.util.FileUtils.FileAction;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ProfileReference implements Displayable {

	private static final String DEFAULT_PROFILE_NAME = "Default";

	private static List<ProfileReference> profiles = new UniqList<ProfileReference>() {

		private static final long serialVersionUID = 1L;

		public boolean contains(Object o) {
			if (o instanceof String) {
				for (ProfileReference reference : this) {
					if (reference.getName().equals(o)) {
						return true;
					} else if (reference.toString().equals(o)) {
						return true;
					}
				}
			} else if (o instanceof ProfileReference) {
				for (ProfileReference reference : this) {
					if (reference.getName() == o) {
						return true;
					} else if (reference.getName() == ((ProfileReference) o).getName()) {
						return true;
					}
				}
			}
			return super.contains(o);
		};
	};

	private final UUID uuid;
	private String name;

	public ProfileReference(String name) {
		this.name = name;
		this.uuid = UUID.randomUUID();
		updateDisplayProperty();
	}

	public ProfileReference(UUID uuid, String name) {
		this.uuid = uuid;
		this.name = name;
		updateDisplayProperty();
	}

	public String getName() {
		return name;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setName(String name) {
		this.name = name;
		updateDisplayProperty();
	}

	public static ProfileReference getReference(UUID profile) {
		for (ProfileReference ref : profiles) {
			if (ref.uuid.equals(profile)) {
				return ref;
			}
		}
		return null;
	}

	public static List<ProfileReference> getProfiles() {
		return profiles;
	}

	/**
	 * Create and Save a new Profile
	 * 
	 * @param name
	 *            Profile Name
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static Profile newProfile(String name) throws UnsupportedEncodingException, IOException {
		ProfileReference ref = new ProfileReference(UUID.randomUUID(), name);
		ProfileReference.addProfile(ref);

		Profile profile = new Profile(ref);
		profile.save();

		return profile;
	}

	public static void addProfile(ProfileReference ref) {
		profiles.add(ref);

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
	 * Duplicate one profile on drive. To use the new profile, it must be load manually.
	 * 
	 * 
	 * @param src
	 *            Name of the original Profile
	 * @param des
	 *            Name of the new Profile
	 * @throws IOException
	 */
	public static void duplicate(ProfileReference src, ProfileReference des) throws IOException {
		if (!des.equals(src)) {
			FileUtils.loopThroughDirectory(ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, src.getFileName()),
					new FileAction() {

						@Override
						public void onFile(Path file) throws IOException {
							String name = file.getFileName().toString();
							Path desPath = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, des.getFileName(), name);
							if (Files.notExists(desPath.getParent())) {
								Files.createDirectories(desPath.getParent());
							}
							Files.copy(file, desPath, StandardCopyOption.REPLACE_EXISTING);
						}

						@Override
						public void onDirectory(Path file) throws IOException {}
					});

			profiles.add(des);
		}
	}

	public static void removeProfile(ProfileReference ref) throws IOException {
		profiles.remove(ref);

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

	private static final String ROOT_ELEMENT = "Settings";
	private static final String PROFILE_ELEMENT = "Profile";
	private static final String UUID_ATTR = "uuid";
	private static final String NAME_ATTR = "name";

	public static void loadProfiles() throws Exception {
		profiles.clear();

		Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "Profiles.xml");

		if (Files.exists(path)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(path));
			Element root = document.getRootElement();
			for (Object object : root.elements(PROFILE_ELEMENT)) {
				Element element = (Element) object;

				UUID uuid = UUID.fromString(element.attributeValue(UUID_ATTR));
				String name = element.attributeValue(NAME_ATTR);

				ProfileReference ref = new ProfileReference(uuid, name);
				profiles.add(ref);
			}
		}

		if (profiles.isEmpty()) {
			Profile profile = newProfile(DEFAULT_PROFILE_NAME);
			profile.save();
		}
	}

	public static void saveProfiles() throws UnsupportedEncodingException, IOException {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement(ROOT_ELEMENT);

		for (ProfileReference ref : profiles) {
			Element element = root.addElement(PROFILE_ELEMENT);

			element.addAttribute(UUID_ATTR, ref.uuid.toString());
			element.addAttribute(NAME_ATTR, ref.name);
		}

		Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "Profiles.xml");
		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}

	public Path getCustomFilePath(String name) {
		return ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, getFileName(), name);
	}

	public String getFileName() {
		return uuid.toString();
	}

	@Override
	public String toString() {
		return name;
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
