package de.tobias.playpad.project;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.sun.nio.zipfs.ZipFileSystem;

import de.tobias.playpad.settings.ProfileReference;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.FileUtils;
import de.tobias.utils.util.FileUtils.FileAction;

public class ProjectImporter {

	public static ProjectReference importProject(Path zipFile, ProfileChooseable chooseable, Importable importable)
			throws IOException, DocumentException {
		ZipFileSystem fileSystem = (ZipFileSystem) FileSystems.newFileSystem(zipFile, null);
		App app = ApplicationUtils.getApplication();

		Path infoPath = fileSystem.getPath("info.xml");
		if (Files.exists(infoPath)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(infoPath));
			Element rootElement = document.getRootElement();

			UUID projectUUID = null;
			String projectName = null;

			boolean includeProfile = false;
			UUID profileUUID = null;
			String profileName = null;

			boolean includeMedia = false;

			Element projectElement = rootElement.element("Project");
			if (projectElement != null) {
				projectUUID = UUID.fromString(projectElement.attributeValue("uuid"));
				projectName = projectElement.attributeValue("name");
			}

			Element profileElement = rootElement.element("Profile");
			if (profileElement != null) {
				includeProfile = Boolean.valueOf(profileElement.attributeValue("export"));
				profileUUID = UUID.fromString(profileElement.attributeValue("uuid"));
				profileName = profileElement.attributeValue("name");
			}

			Element mediaElement = rootElement.element("Media");
			if (mediaElement != null) {
				includeMedia = Boolean.valueOf(mediaElement.attributeValue("export"));
			}

			// Import Profile
			UUID localProfileUUID = null;
			if (includeProfile) {
				localProfileUUID = UUID.randomUUID();
				String localProfileUUIDString = localProfileUUID.toString();

				Path profileFolder = fileSystem.getPath(profileUUID.toString());
				Path localFolder = app.getPath(PathType.CONFIGURATION, localProfileUUID.toString());
				Files.createDirectories(localFolder);

				FileUtils.loopThroughDirectory(profileFolder, new FileAction() {

					@Override
					public void onFile(Path file) throws IOException {
						Files.copy(file, app.getPath(PathType.CONFIGURATION, localProfileUUIDString, file.getFileName().toString()));
					}

					@Override
					public void onDirectory(Path file) throws IOException {}
				});

				if (ProfileReference.getProfiles().contains(profileName)) {
					profileName = importable.replaceProfile(profileName);
				}

				ProfileReference profileRef = new ProfileReference(localProfileUUID, profileName);
				ProfileReference.addProfile(profileRef);

			} else {
				localProfileUUID = chooseable.getUnkownProfile().getRef().getUuid();
			}

			// Import Project
			if (projectUUID != null) {
				UUID localProjectUUID = UUID.randomUUID();

				Path projectFile = fileSystem.getPath(projectUUID.toString() + Project.FILE_EXTENSION);
				Path localFile = app.getPath(PathType.DOCUMENTS, localProjectUUID + Project.FILE_EXTENSION);

				Files.copy(projectFile, localFile);

				if (ProjectReference.getProjects().contains(projectName)) {
					projectName = importable.replaceProject(projectName);
				}

				ProjectReference projectRef = new ProjectReference(localProjectUUID, projectName,
						ProfileReference.getReference(localProfileUUID));
				ProjectReference.addProject(projectRef);

				// Import Media
				if (includeMedia) {
					Path mediaPath = importable.mediaFolder();
					Project.importMedia(projectRef, fileSystem, mediaPath);
				}

				return projectRef;
			}
		}
		return null;
	}
}
