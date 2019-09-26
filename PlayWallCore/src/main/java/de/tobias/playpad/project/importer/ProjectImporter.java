package de.tobias.playpad.project.importer;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.util.zip.ZipFile;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ProjectReader.ProjectReaderDelegate.ProfileAbortException;
import de.tobias.playpad.project.loader.ProjectLoader;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by tobias on 11.03.17.
 */
public class ProjectImporter {

	public static class ProjectImportCorruptedException extends Exception {

	}

	private ProjectImporterDelegate delegate;

	private ZipFile zip;

	private String projectName;
	private String profileName;

	private boolean includeProfile;
	private boolean includeMedia;

	private UUID projectUUID;
	private UUID profileUUID;

	private ProjectReference importedProjectReference;

	public ProjectImporter(Path zipFile, ProjectImporterDelegate delegate) throws IOException,
			ProjectImportCorruptedException {
		this.delegate = delegate;

		loadZipFile(zipFile);
	}

	public void execute() throws IOException, ProjectNotFoundException, DocumentException, ProfileNotFoundException,
			ProfileAbortException, ProjectImportCorruptedException {
		if (includeProfile && delegate.shouldImportProfile()) {
			importProfile();
		}

		importedProjectReference = importProjectFile();

		if (includeMedia && delegate.shouldImportMedia()) {
			importMedia(loadMediaPaths());
		} else {
			replaceMediaPathIds(loadMediaPaths());
		}
	}

	private void loadZipFile(Path zipFile) throws IOException, ProjectImportCorruptedException {
		zip = new ZipFile(zipFile, ZipFile.FileMode.READ);

		// Load Zip meta information
		InputStream infoInputStream = zip.inputStream("info.xml");
		if (infoInputStream == null) {
			throw new ProjectImportCorruptedException();
		}

		try {
			SAXReader reader = new SAXReader();
			Document document = reader.read(infoInputStream);
			Element rootElement = document.getRootElement();

			Element projectElement = rootElement.element("Project");
			if (projectElement != null) {
				projectUUID = UUID.fromString(projectElement.attributeValue("uuid"));
				projectName = projectElement.attributeValue("name");
			}

			Element profileElement = rootElement.element("Profile");
			if (profileElement != null) {
				includeProfile = Boolean.parseBoolean(profileElement.attributeValue("export"));
				profileUUID = UUID.fromString(profileElement.attributeValue("uuid"));
				profileName = profileElement.attributeValue("name");
			}

			Element mediaElement = rootElement.element("Media");
			if (mediaElement != null) {
				includeMedia = Boolean.parseBoolean(mediaElement.attributeValue("export"));
			}
		} catch (DocumentException e) {
			throw new ProjectImportCorruptedException();
		}
	}

	private List<MediaPath> loadMediaPaths() throws IOException, ProjectImportCorruptedException {
		// Load Zip meta information
		InputStream infoInputStream = zip.inputStream("media.xml");
		if (infoInputStream == null) {
			throw new ProjectImportCorruptedException();
		}

		try {
			SAXReader reader = new SAXReader();
			Document document = reader.read(infoInputStream);
			Element rootElement = document.getRootElement();

			List<MediaPath> mediaPathList = new ArrayList<>();

			for (Element pathElement : rootElement.elements("Path")) {
				UUID uuid = UUID.fromString(pathElement.attributeValue("uuid"));
				Path path = Paths.get(pathElement.attributeValue("path"));

				MediaPath mediaPath = new MediaPath(uuid, path, null);
				mediaPathList.add(mediaPath);
			}
			return mediaPathList;
		} catch (DocumentException e) {
			throw new ProjectImportCorruptedException();
		}
	}

	private void replaceMediaPathIds(List<MediaPath> mediaPaths) throws ProjectNotFoundException, ProfileAbortException,
			ProfileNotFoundException, DocumentException, IOException {
		ProjectLoader loader = new ProjectLoader(importedProjectReference);
		loader.setLoadMedia(false);
		Project project = loader.load();

		for (Pad pad : project.getPads()) {
			Iterator<MediaPath> iterator = pad.getPaths().iterator();
			while (iterator.hasNext()) {
				MediaPath oldMediaPath = iterator.next();
				find(mediaPaths, oldMediaPath.getId()).ifPresent(result -> {
					try {
						pad.removePathListener(oldMediaPath);
						iterator.remove();

						pad.setPath(result.getPath());
					} catch (NullPointerException e) {
						Logger.error("Import Error on Pad: " + pad.getUuid());
					}
				});
			}
		}
	}

	private Optional<MediaPath> find(List<MediaPath> mediaPaths, UUID id) {
		return mediaPaths.parallelStream().filter(i -> i.getId().equals(id)).findFirst();
	}

	private ProjectReference importProjectFile() throws IOException {
		String newProjectName = delegate.getProjectName();
		boolean sync = delegate.shouldProjectSynced();

		ProfileReference profileReference = ProfileReferenceManager.getReference(profileUUID);
		ProjectReference reference = ProjectReferenceManager.addProject(newProjectName, profileReference, sync);

		Path projectFile = Paths.get(projectUUID.toString() + Project.FILE_EXTENSION); // ZIP File
		Path localFile = reference.getProjectPath();

		zip.getFile(projectFile, localFile);

		ProjectReferenceManager.addProjectReference(reference);
		return reference;
	}

	private void importProfile() throws IOException {
		App app = ApplicationUtils.getApplication();
		UUID localProfileUUID = UUID.randomUUID();
		String localProfileUUIDString = localProfileUUID.toString();

		Path profileFolder = Paths.get(profileUUID.toString()); // ZIP Folder

		Path localFolder = app.getPath(PathType.CONFIGURATION, localProfileUUID.toString());
		Files.createDirectories(localFolder);
		Logger.info("Create new profile for import: " + localProfileUUID);

		zip.stream().filter(entry -> entry.getName().startsWith(profileFolder.toString())).forEach(entry ->
		{
			String name = entry.getName().substring(entry.getName().lastIndexOf('/') + 1);
			try {
				Path dest = app.getPath(PathType.CONFIGURATION, localProfileUUIDString, name);
				zip.getFile(Paths.get(entry.getName()), dest);
				Logger.info("Copied Profile Data: \"" + entry.getName() + "\" to location: " + dest);
			} catch (Exception e) {
				Logger.error(e);
			}
		});

		ProfileReference profileRef = new ProfileReference(localProfileUUID, delegate.getProfileName());
		ProfileReferenceManager.addProfile(profileRef);

		profileUUID = localProfileUUID; // Update Profile UUID with new local profile uuid
	}

	private void importMedia(List<MediaPath> mediaPaths) throws ProjectNotFoundException, ProfileNotFoundException, DocumentException,
			IOException, ProfileAbortException {
		Path folder = delegate.getMediaPath();

		ProjectLoader loader = new ProjectLoader(importedProjectReference);
		loader.setLoadMedia(false);
		Project project = loader.load();

		for (Pad pad : project.getPads()) {
			for (MediaPath path : pad.getPaths()) {
				Optional<MediaPath> result = find(mediaPaths, path.getId());

				if (result.isPresent()) {
					String fileName = result.get().getFileName();
					Path zipMediaFile = Paths.get("/media").resolve(fileName);
					Path newMediaPath = folder.resolve(fileName);

					zip.getFile(zipMediaFile, newMediaPath);
					path.setPath(newMediaPath, false);
				}
			}
		}
		ProjectReferenceManager.saveSingleProject(project);
	}

	public ProjectReference getProjectReference() {
		return importedProjectReference;
	}

	// Information Getter
	public String getProjectName() {
		return projectName;
	}

	public String getProfileName() {
		return profileName;
	}

	public boolean isIncludeProfile() {
		return includeProfile;
	}

	public boolean isIncludeMedia() {
		return includeMedia;
	}
}
