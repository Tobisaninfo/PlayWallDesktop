package de.tobias.playpad.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.settings.ProfileReference;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.FileUtils;
import de.tobias.utils.util.FileUtils.FileAction;
import de.tobias.utils.util.ZipFile;
import de.tobias.utils.util.ZipFile.FileMode;

public class ProjectExporter {

	public static final String mediaFolder = "Media";

	public static void exportProject(ProjectReference projectRef, Path zipFile, boolean includeProfile, boolean includeMedia, ExportView view)
			throws IOException {

		// Delete Esisting Zip File
		if (Files.exists(zipFile))
			Files.delete(zipFile);

		ZipFile zip = new ZipFile(zipFile, FileMode.WRITE);
		App application = ApplicationUtils.getApplication();

		// Export Profile
		ProfileReference profileRef = projectRef.getProfileReference();
		if (includeProfile) {
			String profileFileName = profileRef.getFileName();
			Path profilePath = application.getPath(PathType.CONFIGURATION, profileFileName);

			// Copy
			FileUtils.loopThroughDirectory(profilePath, new FileAction() {

				@Override
				public void onFile(Path file) throws IOException {
					zip.addFile(file, Paths.get(profileFileName, file.getFileName().toString()));
				}

				@Override
				public void onDirectory(Path file) throws IOException {}
			});
		}

		// Export Project
		Path projectLocalPath = application.getPath(PathType.DOCUMENTS, projectRef.getFileName());
		zip.addFile(projectLocalPath, Paths.get(projectRef.getFileName()));

		if (includeMedia) {
			try {
				exportMedia(projectRef, zip, view);
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

		zip.addFile(Paths.get("info.xml"), os ->
		{
			try {
				XMLWriter writer = new XMLWriter(os, OutputFormat.createPrettyPrint());
				writer.write(infoDocument);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		zip.close();
	}

	/**
	 * Load an project internal, so that each PadContent cloud export the needed medai into the zip filesystems. Each PadContent can read
	 * the save path from the Element object.
	 * 
	 * @param ref
	 *            Project to export
	 * @param des
	 *            Destination zip file
	 * @throws DocumentException
	 * @throws IOException
	 */
	private static void exportMedia(ProjectReference ref, ZipFile zip, ExportView view) throws DocumentException, IOException {
		Path projectPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, ref.getFileName());
		SAXReader reader = new SAXReader();
		Document document = reader.read(Files.newInputStream(projectPath));

		Element rootElement = document.getRootElement();
		List<?> elements = rootElement.elements(Project.PAD_ELEMENT);

		view.setTasks(elements.size());

		for (Object padObj : elements) {
			if (padObj instanceof Element) {
				Element padElement = (Element) padObj;

				Pad pad = new Pad(null, padElement); // Null für Project, da das pad nicht weiter verwendet wird
				if (pad.getContent() != null) {
					pad.getContent().exportMedia(zip, padElement.element(Pad.CONTENT_ELEMENT));
				}
			}

			view.tastComplete();
		}
	}

	public static interface ExportView {

		public void tastComplete();

		public void setTasks(int vlaue);
	}
}
