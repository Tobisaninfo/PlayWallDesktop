package de.tobias.playpad.action.mapper.listener;

import java.util.List;

import javax.sound.midi.MidiMessage;

import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.InputType;
import de.tobias.playpad.action.MappingUtils;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.midi.MidiListener;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.application.Platform;

/**
 * Diese Klasse Verwaltet den MIDI Input und führt die Actions aus.
 * 
 * @author tobias
 *
 */
public class MidiHandler implements MidiListener {

	private final Midi midi;

	private IMainViewController mainView;
	private Project project;

	public MidiHandler(Midi midi, IMainViewController mainView, Project project) {
		this.midi = midi;
		this.mainView = mainView;
		this.project = project;
	}

	/**
	 * Midi Input Listener
	 */
	@Override
	public void onMidiAction(MidiMessage message) {
		int cmd = message.getMessage()[0];
		int key = message.getMessage()[1];

		// Custom Midi Listener
		midi.getMidiDevice().ifPresent(device -> device.onMidiMessage(message));

		InputType type;
		if (message.getMessage()[2] != 0) {
			type = InputType.PRESSED;
		} else {
			type = InputType.RELEASED;
		}

		Platform.runLater(() ->
		{
			List<Action> actions = MappingUtils.getActionsForMidi(cmd, key, Profile.currentProfile().getMappings().getActiveMapping());
			for (Action action : actions) {
				action.performAction(type, project, mainView);
			}
		});
	}

	public void setProject(Project project) {
		this.project = project;
	}
}