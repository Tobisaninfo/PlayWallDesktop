package de.tobias.playpad.project;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.settings.ProfileReference;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ProjectReference implements Displayable {

	private static List<ProjectReference> projects = new ArrayList<ProjectReference>() {

		private static final long serialVersionUID = 1L;

		public boolean contains(Object o) {
			if (o instanceof String) {
				for (ProjectReference reference : this) {
					if (reference.getName().equals(o)) {
						return true;
					} else if (reference.toString().equals(o)) {
						return true;
					}
				}
			} else if (o instanceof ProjectReference) {
				for (ProjectReference reference : this) {
					if (reference.getName() == o) {
						return true;
					} else if (reference.getName() == ((ProjectReference) o).getName()) {
						return true;
					}
				}
			}
			return super.contains(o);
		};
	};
	private static boolean loadedProjectOverview = false;

	/**
	 * Name + XML
	 */
	private UUID uuid;
	private String name;

	private ProfileReference profileReference;

	private long size;
	private long lastMofied;

	public ProjectReference(UUID uuid, String name, ProfileReference profileReference) {
		this.uuid = uuid;
		this.name = name;
		this.lastMofied = System.currentTimeMillis();
		this.size = 0;
		this.profileReference = profileReference;
		updateDisplayProperty();
	}

	public ProjectReference(UUID uuid, String name, long size, long lastMofied, ProfileReference profileReference) {
		this.uuid = uuid;
		this.name = name;
		this.size = size;
		this.lastMofied = lastMofied;
		this.profileReference = profileReference;
		updateDisplayProperty();
	}

	public UUID getUuid() {
		return uuid;
	}

	public String getName() {
		return name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void setLastMofied(long lastMofied) {
		this.lastMofied = lastMofied;
	}

	public long getLastMofied() {
		return lastMofied;
	}

	public void setName(String name) {
		this.name = name;
		updateDisplayProperty();
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

	public static void addProject(ProjectReference item) throws UnsupportedEncodingException, IOException {
		if (!projects.contains(item)) {
			projects.add(item);
		}
		saveProjects();
	}

	public static void removeDocument(ProjectReference projectReference) throws DocumentException, IOException {
		Path path = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, projectReference.getUuid() + Project.FILE_EXTENSION);

		Files.deleteIfExists(path); // DIRVE
		projects.remove(projectReference); // MODEL
		saveProjects();
	}

	public static ProjectReference duplicate(ProjectReference currentProject, String name) throws IOException {
		ProjectReference newProjectReference = new ProjectReference(UUID.randomUUID(), name, currentProject.getProfileReference());
		addProject(newProjectReference);

		Path oldPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, currentProject.getName());
		Path newPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, newProjectReference.getName());
		Files.copy(oldPath, newPath, StandardCopyOption.COPY_ATTRIBUTES);

		saveProjects();
		return newProjectReference;
	}

	public static List<ProjectReference> getProjects() {
		if (!loadedProjectOverview)
			try {
				loadProjects();
			} catch (DocumentException | IOException e) {
				e.printStackTrace();
			}
		return projects;
	}

	private static final String UUID_ATTR = "uuid";
	private static final String NAME_ATTR = "name";
	private static final String PROFILE_ATTR = "profile";

	public static void loadProjects() throws DocumentException, IOException {
		projects.clear();

		Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "Projects.xml");

		if (Files.exists(path)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(path));
			Element root = document.getRootElement();

			for (Object object : root.elements("Project")) {
				Element element = (Element) object;

				UUID uuid = UUID.fromString(element.attributeValue(UUID_ATTR));
				String name = element.attributeValue(NAME_ATTR);
				UUID profile = UUID.fromString(element.attributeValue(PROFILE_ATTR));

				ProfileReference profileRef = ProfileReference.getReference(profile);
				ProjectReference ref = new ProjectReference(uuid, name, profileRef);

				Path projectPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, ref.getFileName());
				if (Files.exists(projectPath)) {
					ref.setLastMofied(Files.getLastModifiedTime(projectPath).toMillis());
					ref.setSize(Files.size(projectPath));
				}

				projects.add(ref);
			}

			loadedProjectOverview = true;
		}
	}

	public static void saveProjects() throws UnsupportedEncodingException, IOException {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("Settings");

		for (ProjectReference project : projects) {
			Element projectElement = root.addElement("Project");
			projectElement.addAttribute(UUID_ATTR, project.uuid.toString());
			projectElement.addAttribute(NAME_ATTR, project.name);
			projectElement.addAttribute(PROFILE_ATTR, project.profileReference.getUuid().toString());
		}

		Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "Projects.xml");

		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}

	public static List<ProjectReference> getProjectsSorted() {
		if (!loadedProjectOverview)
			try {
				loadProjects();
			} catch (DocumentException | IOException e) {
				e.printStackTrace();
			}

		List<ProjectReference> items = new ArrayList<>();
		projects.forEach(item -> items.add(item));
		items.sort((ProjectReference o1, ProjectReference o2) ->
		{
			return Long.compare(o2.lastMofied, o1.lastMofied);
		});
		return items;
	}

	public String getFileName() {
		return uuid + Project.FILE_EXTENSION;
	}

	public static ProjectReference getProject(UUID project) {
		for (ProjectReference ref : projects) {
			if (ref.uuid.equals(project)) {
				return ref;
			}
		}
		return null;
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
