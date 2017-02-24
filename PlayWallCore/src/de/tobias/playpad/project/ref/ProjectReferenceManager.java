package de.tobias.playpad.project.ref;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.*;
import de.tobias.playpad.server.ConnectionState;
import de.tobias.playpad.server.LoginException;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.Worker;
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

	public static Project loadProject(ProjectReference projectReference, ProjectReader.ProjectReaderDelegate delegate) throws DocumentException, ProfileNotFoundException, IOException, ProjectNotFoundException {
		Server server = PlayPadPlugin.getServerHandler().getServer();

		ProjectReader reader;
		if (projectReference.isSync() && server.getConnectionState() == ConnectionState.CONNECTED) {
			reader = new ProjectSyncReader();
		} else {
			reader = new ProjectSerializer();
		}
		Project project = reader.read(projectReference, delegate);
		Worker.runLater(project::loadPadsContent);
		return project;
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
		ProjectSerializer.save(project);

		// Save To Cloud
		if (ref.isSync()) {
			CommandManager.execute(Commands.PROJECT_ADD, project);
		}

		// Add to Project List
		if (!projects.contains(ref)) {
			projects.add(ref);
		}
		saveProjects();

		return ref;
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
			CommandManager.execute(Commands.PROJECT_REMOVE, projectReference); // Cloud
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
	private static final String PROJECT_ELEMENT = "Project";
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
		projects.forEach(items::add);
		items.sort((o1, o2) -> Long.compare(o2.getLastModified(), o1.getLastModified()));
		return items;
	}
}
