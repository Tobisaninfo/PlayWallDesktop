package de.tobias.playpad.project;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.settings.ProfileReference;
import de.tobias.playpad.xml.XMLHandler;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ProjectReference implements Displayable {

	// Verwaltung
	private static List<ProjectReference> projects = new ProjectReferenceList();
	private static boolean loadedProjectOverview = false;

	// Eigentliche ProjectReference
	private UUID uuid;
	private String name;

	private Set<Module> requestedModules;
	private ProfileReference profileReference;

	// Temp
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

	public ProjectReference(UUID uuid, String name, ProfileReference profileReference, Set<Module> requestedModules) {
		this(uuid, name, profileReference);
		this.requestedModules = requestedModules;
		updateDisplayProperty();
	}

	public ProjectReference(UUID uuid, String name, long size, long lastMofied, ProfileReference profileReference,
			Set<Module> requestedModules) {
		this(uuid, name, profileReference, requestedModules);
		this.size = size;
		this.lastMofied = lastMofied;
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

	public Set<Module> getRequestedModules() {
		return requestedModules;
	}
	
	public void addRequestedModule(Module module) {
		this.requestedModules.add(module);
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

	public List<Module> getMissedModules() {
		List<Module> missedModules = new ArrayList<>();
		Collection<Module> activeModules = PlayPadPlugin.getImplementation().getModules();
		for (Module requested : requestedModules) {
			if (!activeModules.contains(requested)) {
				missedModules.add(requested);
			}
		}
		return missedModules;
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

		duplicateFiles(currentProject, newProjectReference);

		saveProjects();
		return newProjectReference;
	}

	private static void duplicateFiles(ProjectReference currentProject, ProjectReference newProjectReference) throws IOException {
		Path oldPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, currentProject.getName());
		Path newPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, newProjectReference.getName());
		Files.copy(oldPath, newPath, StandardCopyOption.COPY_ATTRIBUTES);
	}

	// Load and Save
	private static final String FILE_NAME = "Projects.xml";
	private static final String PROJECT_ELEMENT = "Project";
	private static final String ROOT_ELEMENT = "Settings";

	public static void loadProjects() throws DocumentException, IOException {
		Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, FILE_NAME);
		if (Files.exists(path)) {
			XMLHandler<ProjectReference> loader = new XMLHandler<>(path);
			projects = loader.loadElements(PROJECT_ELEMENT, new ProjectReferenceSerializer());
		}
		loadedProjectOverview = true;
	}

	public static void saveProjects() throws UnsupportedEncodingException, IOException {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement(ROOT_ELEMENT);

		XMLHandler<ProjectReference> handler = new XMLHandler<>(root);
		handler.saveElements(PROJECT_ELEMENT, projects, new ProjectReferenceSerializer());

		Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, FILE_NAME);
		if (Files.notExists(path)) {
			Files.createDirectories(path.getParent());
			Files.createFile(path);
		}
		XMLHandler.save(path, document);
	}

	/**
	 * Gibt alle Referencen zurück.
	 * 
	 * @return Projectreferenzen
	 */
	public static List<ProjectReference> getProjects() {
		if (!loadedProjectOverview)
			try {
				loadProjects();
			} catch (DocumentException | IOException e) {
				e.printStackTrace();
			}
		return projects;
	}

	/**
	 * Gibt alle Referncen von {@link ProjectReference#getProjects()} aufsteigend nach {@link ProjectReference#getLastMofied()} zurück.
	 * 
	 * @return Projectreferencen
	 */
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

	public Path getProjectPath() {
		App application = ApplicationUtils.getApplication();
		Path projectPath = application.getPath(PathType.DOCUMENTS, getFileName());
		return projectPath;
	}

	public static ProjectReference getProject(UUID project) {
		for (ProjectReference ref : projects) {
			if (ref.uuid.equals(project)) {
				return ref;
			}
		}
		return null;
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
