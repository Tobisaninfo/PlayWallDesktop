package de.tobias.playpad.project.ref;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.*;
import de.tobias.playpad.project.loader.ProjectLoader;
import de.tobias.playpad.server.LoginException;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.xml.XMLHandler;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * List of all projects. Manage adding or removing projects.
 *
 * @author tobias
 * @version 6.2.0
 */
public final class ProjectReferenceManager {

	private ProjectReferenceManager() {
	}

	private static List<ProjectReference> projects = new ProjectReferenceList();
	private static boolean loadedProjectOverview = false;

	/**
	 * Get a project reference with a uuid given.
	 *
	 * @param project project uuid
	 * @return project metadata
	 */
	public static ProjectReference getProject(UUID project) {
		for (ProjectReference ref : projects) {
			if (ref.getUuid().equals(project)) {
				return ref;
			}
		}
		return null;
	}

	public static void saveProject(Project project) throws IOException {
		ProjectWriter writer = new ProjectSerializer();
		writer.write(project);
	}

	/**
	 * Add a new project with a given name and profile reference.
	 *
	 * @param name             project name
	 * @param profileReference linked profile
	 * @param sync             project sync property
	 * @return created projected reference
	 * @throws IOException Failed to save changes to disk
	 */
	public static ProjectReference addProject(String name, ProfileReference profileReference, boolean sync) throws IOException {
		ProjectReference ref = new ProjectReference(UUID.randomUUID(), name, profileReference, sync);
		Project project = new Project(ref);

		// Save To Disk
		saveProject(project);

		// Save To Cloud
		if (ref.isSync()) {
			CommandManager.execute(Commands.PROJECT_ADD, ref, ref);
		}

		addProjectReference(ref);

		return ref;
	}

	public static void addProjectReference(ProjectReference ref) throws IOException {
		// Add to Project List
		if (!projects.contains(ref)) {
			projects.add(ref);
		}
		saveProjects();
	}

	/**
	 * Remove a given project and it's files from disk and cloud.
	 *
	 * @param projectReference project reference
	 * @throws IOException Failed to save changes to disk
	 */
	public static void removeProject(ProjectReference projectReference) throws IOException {
		Path path = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, projectReference.getUuid() + Project.FILE_EXTENSION);

		Files.deleteIfExists(path); // Drive
		projects.remove(projectReference); // Model
		if (projectReference.isSync()) {
			CommandManager.execute(Commands.PROJECT_REMOVE, projectReference, projectReference); // Cloud
		}
		saveProjects();
	}

	/**
	 * Duplicate a given project.
	 *
	 * @param baseProject given project
	 * @param name        new name
	 * @return duplicated project
	 * @throws IOException Failed to save changes to disk
	 */
	public static ProjectReference duplicate(ProjectReference baseProject, String name) throws IOException {
		ProjectReference newProjectReference = new ProjectReference(UUID.randomUUID(), name, baseProject.getProfileReference(), baseProject.isSync());
		projects.add(newProjectReference);
		saveProjects();

		duplicateFiles(baseProject, newProjectReference); // Copy Files

		saveProjects();
		return newProjectReference;
	}

	private static void duplicateFiles(ProjectReference currentProject, ProjectReference newProjectReference) throws IOException {
		Path oldPath = currentProject.getProjectPath();
		Path newPath = newProjectReference.getProjectPath();
		Files.copy(oldPath, newPath, StandardCopyOption.COPY_ATTRIBUTES);
	}

	/**
	 * Load all project references from disk and cloud.
	 *
	 * @return project references
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

	// Load and Save
	private static final String FILE_NAME = "Projects.xml";
	public static final String PROJECT_ELEMENT = "Project";
	private static final String ROOT_ELEMENT = "Settings";

	public static void loadProjects() throws DocumentException, IOException {
		Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, FILE_NAME);
		if (Files.exists(path)) {
			XMLHandler<ProjectReference> loader = new XMLHandler<>(path);
			projects = loader.loadElements(PROJECT_ELEMENT, new ProjectReferenceSerializer());
		}

		Server server = PlayPadPlugin.getServerHandler().getServer();
		try {
			List<ProjectReference> syncedProjects = server.getSyncedProjects();

			// Add new synced projects in client
			for (ProjectReference project : syncedProjects) {
				if (projects.contains(project)) {
					project.setSync(true);
				} else {
					projects.add(project);
				}
			}

			// Remove old projects from client
			List<ProjectReference> removeProjects = new ArrayList<>();

			for (ProjectReference project : projects) {
				if (project.isSync()) {
					if (!syncedProjects.contains(project)) {
						removeProjects.add(project);
					}
				}
			}
			for (ProjectReference project : removeProjects) {
				removeProject(project);
			}

		} catch (IOException ignored) {
		} catch (LoginException e) {
			e.printStackTrace();
		}

		saveProjects();
		loadedProjectOverview = true;
	}

	public static void saveProjects() throws IOException {
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
		items.addAll(projects);
		items.sort((o1, o2) -> Long.compare(o2.getLastModified(), o1.getLastModified()));
		return items;
	}

	public static void setSync(ProjectReference reference, boolean newValue) throws ProjectNotFoundException, ProfileNotFoundException, DocumentException, IOException, ProjectReader.ProjectReaderDelegate.ProfileAbortException {
		if (newValue) {
			ProjectLoader loader = new ProjectLoader(reference);
			Project project = loader.load();

			CommandManager.execute(Commands.PROJECT_ADD, reference, reference);

			Server server = PlayPadPlugin.getServerHandler().getServer();
			server.postProject(project);
		} else {
			CommandManager.execute(Commands.PROJECT_REMOVE, reference, reference);
		}

		reference.setSync(newValue);
	}

	public static boolean validateProjectName(ProjectReference reference, String newValue) {
		return getProjects().stream().filter(r -> r != reference).noneMatch(p -> p.getName().equals(newValue));
	}

	public static boolean validateProjectName(String newValue) {
		return validateProjectName(null, newValue);
	}
}
