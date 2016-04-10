package de.tobias.playpad.project;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import de.tobias.playpad.settings.ProfileReference;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.FileUtils;
import de.tobias.utils.util.FileUtils.FileAction;

public class ProjectExporter {
	
	public static final String mediaFolder = "Media";

	public static void exportProject(ProjectReference projectRef, Path zipFile, boolean includeProfile, boolean includeMedia)
			throws IOException {
		URI p = Paths.get(zipFile.toString()).toUri();
		URI uri = URI.create("jar:" + p);

		Map<String, String> env = new HashMap<>();
		env.put("create", "true");
		env.put("encoding", "UTF-8");

		// Delete Esisting Zip File
		if (Files.exists(zipFile))
			Files.delete(zipFile);

		FileSystem zipfs = FileSystems.newFileSystem(uri, env);
		App application = ApplicationUtils.getApplication();

		// Export Profile
		ProfileReference profileRef = projectRef.getProfileReference();
		if (includeProfile) {
			String profileFileName = profileRef.getFileName();
			Path profilePath = application.getPath(PathType.CONFIGURATION, profileFileName);

			// Copy
			Files.createDirectories(zipfs.getPath(profileFileName));
			FileUtils.loopThroughDirectory(profilePath, new FileAction() {

				@Override
				public void onFile(Path file) throws IOException {
					Files.copy(file, zipfs.getPath(profileFileName, file.getFileName().toString()));
				}

				@Override
				public void onDirectory(Path file) throws IOException {}
			});
		}

		// Export Project
		Files.copy(application.getPath(PathType.DOCUMENTS, projectRef.getFileName()), zipfs.getPath(projectRef.getFileName()));

		if (includeMedia) {
			try {
				Project.exportMedia(projectRef, zipfs);
			} catch (DocumentException e) {
				e.printStackTrace();
			}
		}

		Document infoDocument = DocumentHelper.createDocument();

		Element rootElement = infoDocument.addElement("Info");
		rootElement.addElement("Profile").addAttribute("export", String.valueOf(includeProfile))
				.addAttribute("uuid", profileRef.getUuid().toString()).addAttribute("name", profileRef.getName());
		rootElement.addElement("Project").addAttribute("uuid", projectRef.getUuid().toString()).addAttribute("name", projectRef.getName());
		rootElement.addElement("Media").addAttribute("export", String.valueOf(includeMedia));

		XMLWriter writer = new XMLWriter(Files.newOutputStream(zipfs.getPath("info.xml")), OutputFormat.createPrettyPrint());
		writer.write(infoDocument);
		writer.close();

		zipfs.close();
	}
}
