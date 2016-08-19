package de.tobias.playpad.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadException;
import de.tobias.playpad.pad.PadSerializer;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.utils.xml.XMLHandler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Hold all information about the pads and it's settings.
 * 
 * @author tobias
 *
 */
public class Project {

	/**
	 * Pattern für den Namen des Projekts
	 */
	public static final String PROJECT_NAME_PATTERN = "\\w{1}[\\w\\s-_]{0,}";
	/**
	 * Dateiendung für eine projekt Datei
	 */
	public static final String FILE_EXTENSION = ".xml";

	/**
	 * Die projektreferenz gibt auskunft über den Namen und die UUID des Projektes
	 */
	private ProjectReference ref;
	/**
	 * Liste mit allen Pads.
	 */
	private HashMap<Integer, Pad> pads;

	private ProjectSettings settings;

	/**
	 * Liste mit den aktuellen Laufzeitfehlern.
	 */
	private transient ObservableList<PadException> exceptions;

	/**
	 * Erstellt ein neues leeres Projekt mit einer Referenz.
	 * 
	 * @param ref
	 *            Referenz mit Namen des Projekts.
	 */
	public Project(ProjectReference ref) {
		this.ref = ref;
		this.pads = new HashMap<>();
		this.settings = new ProjectSettings();

		this.exceptions = FXCollections.observableArrayList();
	}

	/**
	 * Gibt die Projekt Referenz zurück. Dazu zählen Name und UUID sowie das zugehörige Profile.
	 * 
	 * @return Referenz.
	 */
	public ProjectReference getRef() {
		return ref;
	}

	// TODO Update in 5.1.0
	/**
	 * Gibt ein Pad an einem Index zurück. Sollte kein pad vorhanden sein (weil null, so wird vorher ein neues erzeugt.)
	 * 
	 * @param index
	 *            Index
	 * @return Pad am Index i
	 */
	public Pad getPad(int index) {
		if (!pads.containsKey(index)) {
			addPadForIndex(index);
		}
		return pads.get(index);
	}

	public Pad getPad(int x, int y, int page) {
		if (x < settings.getColumns() && y < settings.getRows() && page < settings.getPageCount()) {
			int id = (y * settings.getColumns() + x) + page * settings.getColumns() * settings.getRows();
			return getPad(id);
		}
		return null;
	}

	/**
	 * Gibt die Settings des Projectes zurück
	 * 
	 * @return
	 */
	public ProjectSettings getSettings() {
		return settings;
	}

	/**
	 * Erstellt ein neues leeres Pad (mit Referenz zu diesem Projekt) am Index i.
	 * 
	 * @param index
	 *            Index i
	 */
	private void addPadForIndex(int index) {
		pads.put(index, new Pad(this, index));
	}

	/**
	 * Ersetz ein Pad an einem Index i.
	 * 
	 * @param index
	 *            Index i
	 * @param pad
	 *            Neues Pad für den Index i
	 */
	public void setPad(int index, Pad pad) {
		pad.setIndex(index);
		pads.put(index, pad);
	}

	/*
	 * Speichern und Laden
	 */

	private static final String ROOT_ELEMENT = "Project";
	protected static final String PAD_ELEMENT = "Pad";
	private static final String SETTINGS_ELEMENT = "Settings";

	public static Project load(ProjectReference ref, boolean loadMedia, ProfileChooseable profileChooseable)
			throws DocumentException, IOException, ProfileNotFoundException, ProjectNotFoundException, NoSuchComponentException {
		Path projectPath = ref.getProjectPath();

		if (Files.exists(projectPath)) {
			if (ref.getProfileReference() != null) {
				Profile.load(ref.getProfileReference()); // Lädt das entsprechende Profile und aktiviert es
			} else {
				Profile profile = profileChooseable.getUnkownProfile(); // Lädt Profile / Erstellt neues und hat es gleich im Speicher
				ref.setProfileReference(profile.getRef());
			}

			Project project = new Project(ref);

			// Lädt Pads
			XMLHandler<Pad> handler = new XMLHandler<>(projectPath);
			List<Pad> pads = handler.loadElements(PAD_ELEMENT, new PadSerializer(project));

			for (Pad pad : pads) {
				if (loadMedia)
					pad.loadContent();
				project.pads.put(pad.getIndex(), pad);
			}

			// Lädt die Einstellungen
			Element settingsElement = handler.getRootElement().element(SETTINGS_ELEMENT);
			if (settingsElement != null)
				project.settings = ProjectSettings.load(settingsElement);

			return project;
		} else {
			throw new ProjectNotFoundException(ref);
		}
	}

	public void save() throws IOException {
		Path projectPath = ref.getProjectPath();
		Document document = DocumentHelper.createDocument();

		Element rootElement = document.addElement(ROOT_ELEMENT);

		// Speichern der Pads
		XMLHandler<Pad> handler = new XMLHandler<>(rootElement);
		handler.saveElements(PAD_ELEMENT, pads.values(), new PadSerializer());

		// Speichern der Settings
		Element settingsElement = rootElement.addElement(SETTINGS_ELEMENT);
		settings.save(settingsElement);

		if (Files.notExists(projectPath)) {
			Files.createDirectories(projectPath.getParent());
			Files.createFile(projectPath);
		}
		XMLHandler.save(projectPath, document);
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

	public boolean hasPlayedPlayers() {
		return getPlayedPlayers() > 0;
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

	// Load Methods
	public void loadPadsContent() {
		getPads().values().forEach(pad ->
		{
			try {
				pad.loadContent();
			} catch (NoSuchComponentException e) {
				e.printStackTrace();
				// TODO handle exception withon project
			}
		});
	}

	@Override
	public String toString() {
		return ref.getName() + " (" + ref.getUuid() + ")";
	}

	public int getPadCount() {
		return pads.size();
	}
}
