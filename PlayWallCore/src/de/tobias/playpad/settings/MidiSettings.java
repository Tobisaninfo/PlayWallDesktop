package de.tobias.playpad.settings;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import de.tobias.playpad.model.midi.SubAction;
import de.tobias.playpad.model.midi.MidiAction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MidiSettings {

	private ObservableList<MidiPreset> presets;

	private Optional<MidiAction> draftAction = Optional.empty();

	public MidiSettings() {
		presets = FXCollections.observableArrayList();
	}

	public List<SubAction> getSubActionsForMidi(int midi, int page) {
		for (MidiPreset preset : presets)
			if (preset.isActive()) {
				if (preset.getPage() == -1 || preset.getPage() == page) {
					for (MidiAction action : preset.getMidiActions()) {
						if (action.getMidiKey() == midi) {
							return action.getActions();
						}
					}
				}
			}
		return null;
	}
	
	public MidiAction getMidiActionsForMidi(int midi, int page) {
		for (MidiPreset preset : presets)
			if (preset.isActive()) {
				if (preset.getPage() == -1 || preset.getPage() == page) {
					for (MidiAction action : preset.getMidiActions()) {
						if (action.getMidiKey() == midi) {
							return action;
						}
					}
				}
			}
		return null;
	}

	public List<MidiAction> getSettingsForPage(int page) {
		List<MidiAction> actions = new ArrayList<>();
		for (MidiPreset preset : presets) {
			if (preset.isActive()) {
				if (preset.getPage() == -1 || preset.getPage() == page) {
					actions.addAll(preset.getMidiActions());
				}
			}
		}
		return actions;
	}

	public List<MidiPreset> getActivePresets(int page) {
		List<MidiPreset> presets = new ArrayList<>();
		for (MidiPreset preset : this.presets) {
			if (preset.isActive()) {
				if (preset.getPage() == -1 || preset.getPage() == page) {
					presets.add(preset);
				}
			}
		}
		return presets;
	}

	public ObservableList<MidiPreset> getPresets() {
		return presets;
	}

	public Optional<MidiAction> getDraftAction() {
		return draftAction;
	}

	public void setDraftAction(MidiAction draftAction) {
		if (draftAction != null)
			this.draftAction = Optional.of(draftAction);
		else
			this.draftAction = Optional.empty();
	}

	public static MidiSettings load(Path path) throws DocumentException, IOException {
		MidiSettings settings = new MidiSettings();

		if (Files.exists(path)) {
			readMidiSettings(Files.newInputStream(path), settings);
		}

		if (settings.presets.isEmpty()) {
			settings.createDefaultSettings(path);
		}

		return settings;
	}

	private static void readMidiSettings(InputStream stream, MidiSettings settings) throws DocumentException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(stream);

		Element root = document.getRootElement();
		for (Object presetRoot : root.elements("Preset")) {
			MidiPreset preset = MidiPreset.load((Element) presetRoot);
			settings.presets.add(preset);
		}

		// Draft
		Element draftElement = root.element("Draft");
		if (draftElement != null) {
			Optional<MidiAction> action = MidiAction.load(draftElement, null);
			if (action.isPresent())
				settings.draftAction = action;
		}
	}

	public void save(Path path) throws UnsupportedEncodingException, IOException {
		Document document = DocumentHelper.createDocument();

		// Actions
		Element root = document.addElement("Control");
		for (MidiPreset preset : presets) {
			preset.save(root.addElement("Preset"));
		}

		// Draft
		if (draftAction.isPresent()) {
			Element draftElement = root.addElement("Draft");
			draftAction.get().save(draftElement);
		}

		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}

	public MidiPreset importMidiPreset(Path path)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, DocumentException, IOException, NullPointerException {
		SAXReader reader = new SAXReader();
		Document document = reader.read(Files.newInputStream(path));
		MidiPreset preset = MidiPreset.load(document.getRootElement());

		if (preset != null) {
			presets.add(preset);
		} else {
			throw new NullPointerException();
		}
		return preset;
	}

	public void exportMidiPreset(Path path, MidiPreset preset) throws UnsupportedEncodingException, IOException {
		Document document = DocumentHelper.createDocument();

		preset.save(document.addElement("Preset"));

		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}

	public void createDefaultSettings(Path settingsSavePath) throws DocumentException, UnsupportedEncodingException, IOException {
		presets.clear();

		readMidiSettings(MidiSettings.class.getClassLoader().getResourceAsStream("de/tobias/playpad/assets/files/default_midi.xml"), this);
		for (MidiPreset preset : presets) {
			preset.setActive(false);
		}
		
		// Aktiviere erstes Preset, damit überhaupt eins aktiv ist
		presets.get(0).setActive(true);
		save(settingsSavePath);
	}

	public void addPreset(MidiPreset preset) {
		presets.add(preset);
	}
}
