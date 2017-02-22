package de.tobias.playpad.project.importer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import de.tobias.playpad.project.ProfileChooseable;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSerializer;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSerializer;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.settings.Profile;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.ZipFile;
import de.tobias.utils.util.ZipFile.FileMode;

public class ProjectImporter {

	public static ProjectReference importProject(Path zipFile, ProfileChooseable chooseable, Importable importable)
			throws IOException, DocumentException {
		ZipFile zip = new ZipFile(zipFile, FileMode.READ);

		App app = ApplicationUtils.getApplication();

		InputStream infoInputStream = zip.inputStream(Paths.get("info.xml"));
		if (infoInputStream != null) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(infoInputStream);
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
				// Dieser Dialog wird aufgerufen, wenn das Profile bereits existiert, wenn nicht wird es direkt importiert
				if (ProfileReferenceManager.getProfiles().contains(profileName)) {
					profileName = importable.replaceProfile(profileName);
				}

				// Wenn der nUtzer das Profile nicht importieren möchte, weil es bereits vorhanden ist, ist diese Variable durch
				// improable.replaceProfile(String) null
				if (profileName != null) {
					localProfileUUID = UUID.randomUUID();
					String localProfileUUIDString = localProfileUUID.toString();

					Path profileFolder = Paths.get(profileUUID.toString());
					Path localFolder = app.getPath(PathType.CONFIGURATION, localProfileUUID.toString());
					Files.createDirectories(localFolder);
					System.out.println("Create new profile for import: " + localProfileUUID);

					zip.stream().filter(entry -> entry.getName().startsWith(profileFolder.toString())).forEach(entry ->
					{
						String name = entry.getName().substring(entry.getName().lastIndexOf("/") + 1);
						try {
							Path dest = app.getPath(PathType.CONFIGURATION, localProfileUUIDString, name);
							zip.getFile(Paths.get(entry.getName()), dest);
							System.out.println("Copyed Profile Data: \"" + entry.getName() + "\" to location: " + dest);
						} catch (Exception e) {
							e.printStackTrace();
						}
					});

					ProfileReference profileRef = new ProfileReference(localProfileUUID, profileName);
					ProfileReferenceManager.addProfile(profileRef);
				} else {
					Profile profile = chooseable.getUnkownProfile();
					if (profile != null) {
						localProfileUUID = profile.getRef().getUuid();
					}
				}
			} else {
				Profile profile = chooseable.getUnkownProfile();
				if (profile != null) {
					localProfileUUID = profile.getRef().getUuid();
				}
			}

			// Import Project
			if (projectUUID != null) {
				UUID localProjectUUID = UUID.randomUUID();

				Path projectFile = Paths.get(projectUUID.toString() + Project.FILE_EXTENSION);
				Path localFile = app.getPath(PathType.DOCUMENTS, localProjectUUID + Project.FILE_EXTENSION);

				zip.getFile(projectFile, localFile);

				if (ProjectReferenceManager.getProjects().contains(projectName)) {
					projectName = importable.replaceProject(projectName);
				}

				ProfileReference profileReference = ProfileReferenceManager.getReference(localProfileUUID);
				ProjectReference projectRef = ProjectReferenceManager.addProject(projectName, profileReference, true); // TODO Sync Property

				// Import Media
				if (includeMedia) {
					Path mediaPath = importable.mediaFolder();
					if (mediaPath != null)
						importMedia(projectRef, zip, mediaPath);
				}

				return projectRef;
			}
		}
		return null;
	}

	/**
	 * Load an project internal, so that each PadContent cloud import the needed medai from the zip filesystems. Each PadContent must
	 * override the path saves in the Element Object.
	 * 
	 * @param ref
	 *            Project to import
	 * @param destination
	 *            Media Destination
	 * @throws DocumentException
	 * @throws IOException
	 */
	private static void importMedia(ProjectReference ref, ZipFile zip, Path destination) throws DocumentException, IOException {
		Path projectPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, ref.getFileName());
		SAXReader reader = new SAXReader();
		Document document = reader.read(Files.newInputStream(projectPath));

		Element rootElement = document.getRootElement();
		for (Object padObj : rootElement.elements(ProjectSerializer.PAD_ELEMENT)) {
			if (padObj instanceof Element) {
				Element padElement = (Element) padObj;

				PadSerializer serializer = new PadSerializer(null);
				Pad pad = serializer.loadElement(padElement);

				if (pad.getContent() != null) {
					//pad.getContent().importMedia(destination, zip, padElement.element(PadSerializer.CONTENT_ELEMENT)); TODO Import Media
				}
			}
		}
		XMLWriter writer = new XMLWriter(Files.newOutputStream(projectPath), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}
}
