package de.tobias.playpad.action;

import de.tobias.playpad.action.mapper.KeyboardMapper;
import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.action.mapper.MidiMapper;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;

public class MappingUtils {

	public static List<Action> getActionsForKey(KeyCode code, Mapping mapping) {
		List<Action> actions = new ArrayList<>();
		for (Action action : mapping.keySet()) {
			for (Mapper mapper : mapping.get(action)) {
				if (mapper instanceof KeyboardMapper) {
					KeyboardMapper keyMapper = (KeyboardMapper) mapper;
					if (keyMapper.getCode() == code) {
						actions.add(action);
					}
				}
			}
		}
		return actions;
	}

	public static List<Action> getActionsForMidi(int cmd, int key, Mapping mapping) {
		List<Action> actions = new ArrayList<>();
		for (Action action : mapping.keySet()) {
			for (Mapper mapper : mapping.get(action)) {
				if (mapper instanceof MidiMapper) {
					MidiMapper midiMapper = (MidiMapper) mapper;
					if (midiMapper.getCommand() == cmd && midiMapper.getKey() == key) {
						actions.add(action);
					}
				}
			}
		}
		return actions;
	}
}
