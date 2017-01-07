package de.tobias.playpad.project.ref;

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

import de.tobias.playpad.project.Project;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.xml.XMLHandler;

public final class ProjectReferences {

	private ProjectReferences() {}

	private static List<ProjectReference> projects = new ProjectReferenceList();
	private static boolean loadedProjectOverview = false;

	public static ProjectReference getProject(UUID project) {
		for (ProjectReference ref : projects) {
			if (ref.getUuid().equals(project)) {
				return ref;
			}
		}
		return null;
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
		Path oldPath = currentProject.getProjectPath();
		Path newPath = newProjectReference.getProjectPath();
		Files.copy(oldPath, newPath, StandardCopyOption.COPY_ATTRIBUTES);
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

	public static List<ProjectReference> getProjectsSorted() {
		if (!loadedProjectOverview)
			try {
				loadProjects();
			} catch (DocumentException | IOException e) {
				e.printStackTrace();
			}

		List<ProjectReference> items = new ArrayList<>();
		projects.forEach(item -> items.add(item));
		items.sort((o1, o2) -> Long.compare(o2.getLastModified(), o1.getLastModified()));
		return items;
	}
}
