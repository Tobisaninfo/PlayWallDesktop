package de.tobias.playpad.settings;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

import javax.sound.midi.MidiMessage;

import org.dom4j.Element;

import de.tobias.playpad.model.midi.Displayable;
import de.tobias.playpad.model.midi.MidiAction;
import de.tobias.playpad.model.midi.SubAction;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MidiPreset implements Displayable, Cloneable {

	private ObservableList<MidiAction> settings;
	private String name;
	private BooleanProperty activeProperty;
	private int page;
	private boolean partly;

	public MidiPreset() {
		// Default Values
		settings = FXCollections.observableArrayList();
		name = "Preset";
		activeProperty = new SimpleBooleanProperty(false);
		page = -1;
		partly = true;
	}

	public Optional<List<SubAction>> getActionsForMidi(int midiCommand, int midiKey) {
		for (MidiAction action : settings) {
			if (action.getMidiKey() == midiKey && action.getMidiCommand() == midiCommand) {
				return Optional.of(action.getActions());
			}
		}
		return Optional.empty();
	}

	public Optional<MidiAction> getMidiActionForMidi(int midiCommand, int midiKey) {
		for (MidiAction action : settings) {
			if (action.getMidiKey() == midiKey && action.getMidiCommand() == midiCommand) {
				return Optional.of(action);
			}
		}
		return Optional.empty();
	}

	public Optional<MidiAction> getMidiActionForMidi(MidiMessage message) {
		return getMidiActionForMidi(message.getMessage()[0], message.getMessage()[1]);
	}

	public ObservableList<MidiAction> getMidiActions() {
		return settings;
	}

	public void addAction(MidiAction action) {
		this.settings.add(action);
	}

	public void removeAction(MidiAction action) {
		settings.remove(action);
	}

	public void clearActions() {
		settings.clear();
	}

	public boolean isActive() {
		return activeProperty.get();
	}

	public void setActive(boolean active) {
		this.activeProperty.set(active);
	}

	public BooleanProperty activeProperty() {
		return activeProperty;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		displayProperty.set(toString());
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public boolean isPartly() {
		return partly;
	}

	public void setPartly(boolean partly) {
		this.partly = partly;
	}

	public boolean isContaining(int midiCommand, int midiKey) {
		for (MidiAction midiAction : getMidiActions()) {
			if (midiAction.getMidiKey() == midiKey && midiAction.getMidiCommand() == midiCommand) {
				return true;
			}
		}
		return false;
	}

	// Storage
	public static MidiPreset load(Element root) {
		MidiPreset preset = new MidiPreset();

		preset.name = root.attributeValue("name");
		preset.activeProperty.set(Boolean.valueOf(root.attributeValue("active")));
		preset.partly = Boolean.valueOf(root.attributeValue("partly"));
		preset.page = Integer.valueOf(root.attributeValue("page"));

		// Actions
		for (Object element : root.elements("Midi")) {
			Element midiElement = (Element) element;
			Optional<MidiAction> action = MidiAction.load(midiElement, preset);
			if (action.isPresent())
				preset.settings.add(action.get());
		}
		return preset;
	}

	public void save(Element root) throws UnsupportedEncodingException, IOException {
		// Eigenschaften des Presets
		root.addAttribute("name", name);
		root.addAttribute("active", String.valueOf(activeProperty.get()));
		root.addAttribute("partly", String.valueOf(partly));
		root.addAttribute("page", String.valueOf(page));

		// Actions
		for (MidiAction action : settings) {
			Element midiElement = root.addElement("Midi");
			action.save(midiElement);
		}
	}

	@Override
	public String toString() {
		return name;
	}

	private StringProperty displayProperty = new SimpleStringProperty();

	@Override
	public StringProperty displayProperty() {
		displayProperty.set(toString());
		return displayProperty;
	}

	@Override
	public MidiPreset clone() throws CloneNotSupportedException {
		MidiPreset preset = (MidiPreset) super.clone();
		preset.settings = FXCollections.observableArrayList();
		for (MidiAction midiAction : settings) {
			preset.settings.add(midiAction.clone());
		}
		preset.page = page;
		preset.partly = partly;

		preset.name = name;

		preset.displayProperty = new SimpleStringProperty();
		preset.updateString();
		return preset;
	}

	public void updateString() {
		displayProperty.set(toString());
	}
}
