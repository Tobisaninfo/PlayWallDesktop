package de.tobias.playpad.project.export;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.mediapath.MediaPath;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.loader.ProjectLoader;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.FileUtils;
import de.tobias.utils.util.ZipFile;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Created by tobias on 11.03.17.
 */
public class ProjectExporter {

	private ProjectExporterDelegate delegate;

	public ProjectExporter(ProjectExporterDelegate delegate) {
		this.delegate = delegate;
	}

	public void export(Path zipFile, ProjectReference reference, boolean includeProfile, boolean includeMedia) throws IOException, ProjectNotFoundException, DocumentException, ProfileNotFoundException {
		ZipFile zip = new ZipFile(zipFile, ZipFile.FileMode.WRITE);

		exportProject(zip, reference);
		if (includeProfile) {
			exportProfile(zip, reference.getProfileReference());
		}
		if (includeMedia) {
			exportMedia(zip, reference);
		}
		exportInfoFile(zip, reference, includeProfile, includeMedia);

		zip.close();
	}

	private void exportInfoFile(ZipFile zip, ProjectReference projectRef, boolean includeProfile, boolean includeMedia) throws IOException {
		ProfileReference profileRef = projectRef.getProfileReference();
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
	}

	private void exportProject(ZipFile zipFile, ProjectReference projectRef) throws IOException {
		App app = ApplicationUtils.getApplication();
		Path projectLocalPath = app.getPath(PathType.DOCUMENTS, projectRef.getFileName());
		zipFile.addFile(projectLocalPath, Paths.get(projectRef.getFileName()));
	}

	private void exportProfile(ZipFile zip, ProfileReference reference) throws IOException {
		App app = ApplicationUtils.getApplication();

		String profileFileName = reference.getFileName();
		Path profilePath = app.getPath(PathType.CONFIGURATION, profileFileName);

		// Copy
		FileUtils.loopThroughDirectory(profilePath, new FileUtils.FileActionAdapter() {
			@Override
			public void onFile(Path file) throws IOException {
				zip.addFile(file, Paths.get(profileFileName, file.getFileName().toString()));
			}
		});
	}

	private void exportMedia(ZipFile zip, ProjectReference reference) throws ProjectNotFoundException, ProfileNotFoundException, DocumentException, IOException {
		ProjectLoader loader = new ProjectLoader(reference);
		loader.setLoadMedia(false);
		loader.setLoadProfile(false);
		Project project = loader.load();

		Collection<Pad> pads = project.getPads();

		delegate.setTasks(pads.size());

		for (Pad pad : pads) {
			for (MediaPath mediaPath : pad.getPaths()) {
				Path desPath = Paths.get("/media").resolve(mediaPath.getPath().getFileName());
				zip.addFile(mediaPath.getPath(), desPath);
			}
			delegate.taskComplete();
		}
	}
}
