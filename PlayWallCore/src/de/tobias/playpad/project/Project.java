package de.tobias.playpad.project;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadException;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Project {

	public static final String projectNameEx = "\\w{1}[\\w\\s-_]{0,}";
	public static final String FILE_EXTENSION = ".xml";

	private ProjectReference ref;
	private HashMap<Integer, Pad> pads;
	private ObservableList<PadException> exceptions;

	public Project(ProjectReference ref) {
		this.ref = ref;
		this.pads = new HashMap<>();
		this.exceptions = FXCollections.observableArrayList();
	}

	public ProjectReference getRef() {
		return ref;
	}

	public Pad getPad(int index) {
		if (pads.containsKey(index)) {
			return pads.get(index);
		} else {
			pads.put(index, new Pad(this, index)); // Create Pad if not exists
			return pads.get(index);
		}
	}

	public void setPad(int index, Pad pad) {
		pads.put(index, pad);
	}

	public void replacePads(int src, int des) {
		Pad srcPad = getPad(src);
		srcPad.setIndex(des);
		pads.put(des, srcPad);
		pads.remove(src);
	}

	public void movePads(int src, int des) {
		Pad oldPad = getPad(src);
		Pad newPad = getPad(des);

		oldPad.setIndex(des);
		newPad.setIndex(src);

		pads.put(des, oldPad);
		pads.put(src, newPad);
	}

	private static final String ROOT_ELEMENT = "Project";
	private static final String PAD_ELEMENT = "Pad";

	public static Project load(ProjectReference ref, boolean loadMedia, ProfileChooseable profileChooseable)
			throws DocumentException, IOException, ProfileNotFoundException, ProjectNotFoundException {
		Path projectPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, ref.getFileName());
		if (Files.exists(projectPath)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(projectPath));

			if (ref.getProfileReference() != null) {
				Profile.load(ref.getProfileReference()); // Lädt das entsprechende Profile und aktiviert es
			} else {
				Profile profile = profileChooseable.getUnkownProfile(); // Lädt Profile / Erstellt neues und hat es gleich im Speicher
				ref.setProfileReference(profile.getRef());
			}

			Project project = new Project(ref);

			Element rootElement = document.getRootElement();

			for (Object padObj : rootElement.elements(PAD_ELEMENT)) {
				if (padObj instanceof Element) {
					Element padElement = (Element) padObj;

					// Load Pad Settings
					Pad pad = new Pad(project, padElement);

					// Load Media
					if (loadMedia) {
						pad.loadContent();
					}

					project.pads.put(pad.getIndex(), pad);
				}
			}

			return project;
		} else {
			throw new ProjectNotFoundException(ref);
		}
	}

	public void save() throws UnsupportedEncodingException, IOException {
		Path projectPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, ref.getFileName());
		Document document = DocumentHelper.createDocument();

		Element rootElement = document.addElement(ROOT_ELEMENT);
		for (int index : pads.keySet()) {
			Pad pad = pads.get(index);

			Element padElement = rootElement.addElement(PAD_ELEMENT);
			pad.save(padElement);
		}

		if (Files.notExists(projectPath)) {
			Files.createDirectories(projectPath.getParent());
			Files.createFile(projectPath);
		}

		XMLWriter writer = new XMLWriter(Files.newOutputStream(projectPath), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
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
	public static void importMedia(ProjectReference ref, FileSystem zipfs, Path destination) throws DocumentException, IOException {
		Path projectPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, ref.getFileName());
		SAXReader reader = new SAXReader();
		Document document = reader.read(Files.newInputStream(projectPath));

		Element rootElement = document.getRootElement();
		for (Object padObj : rootElement.elements(PAD_ELEMENT)) {
			if (padObj instanceof Element) {
				Element padElement = (Element) padObj;

				Pad pad = new Pad(null, padElement); // Null für Project, da das pad nicht weiter verwendet wird
				if (pad.getContent() != null) {
					pad.getContent().importMedia(destination, zipfs, padElement.element(Pad.CONTENT_ELEMENT));
				}
			}
		}
		XMLWriter writer = new XMLWriter(Files.newOutputStream(projectPath), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
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
	public static void exportMedia(ProjectReference ref, FileSystem des) throws DocumentException, IOException {
		Path projectPath = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, ref.getFileName());
		SAXReader reader = new SAXReader();
		Document document = reader.read(Files.newInputStream(projectPath));

		Element rootElement = document.getRootElement();
		for (Object padObj : rootElement.elements(PAD_ELEMENT)) {
			if (padObj instanceof Element) {
				Element padElement = (Element) padObj;

				Pad pad = new Pad(null, padElement); // Null für Project, da das pad nicht weiter verwendet wird
				if (pad.getContent() != null) {
					pad.getContent().exportMedia(des, padElement.element(Pad.CONTENT_ELEMENT));
				}
			}
		}
	}

	public HashMap<Integer, Pad> getPads() {
		return pads;
	}

	public int getPlayedPlayers() {
		int count = 0;
		for (Pad pad : pads.values()) {
			if (pad.getStatus() == PadStatus.PLAY || pad.getStatus() == PadStatus.PAUSE) {
				count++;
			}
		}
		return count;
	}

	// Exceptions
	public void addException(Pad pad, Path path, Exception exception) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> addException(pad, path, exception));
			return;
		}
		PadException padException = new PadException(pad, path, exception);
		exceptions.add(padException);
	}

	public void removeExceptions(Pad pad) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> removeExceptions(pad));
			return;
		}
		Iterator<PadException> i = exceptions.iterator();
		while (i.hasNext()) {
			PadException exception = i.next();
			if (exception.getPad().equals(pad)) {
				i.remove();
			}
		}
	}

	public void removeException(PadException exception) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> removeException(exception));
			return;
		}
		exceptions.remove(exception);
	}

	public ObservableList<PadException> getExceptions() {
		return exceptions;
	}
}
