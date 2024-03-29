package de.tobias.playpad.action.factory;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.action.ActionHandler;
import de.thecodelabs.midi.feedback.FeedbackType;
import de.thecodelabs.midi.mapping.KeyType;
import de.tobias.playpad.action.ActionProvider;
import de.tobias.playpad.action.ActionType;
import de.tobias.playpad.action.actions.PlaylistNextAction;
import de.tobias.playpad.action.settings.ActionSettingsEntry;
import de.tobias.playpad.action.settings.PlaylistNextActionSettingsEntry;
import de.tobias.playpad.project.ProjectSettings;
import javafx.scene.control.TreeItem;

import java.util.List;

import static de.tobias.playpad.action.actions.PlaylistNextAction.*;

public class PlaylistNextActionProvider extends ActionProvider {

	public PlaylistNextActionProvider(String type) {
		super(TYPE);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public ActionHandler getActionHandler() {
		return new PlaylistNextAction();
	}

	@Override
	public void createDefaultActions(Mapping mapping) {
		for (int x = 0; x < ProjectSettings.MAX_COLUMNS; x++) {
			for (int y = 0; y < ProjectSettings.MAX_ROWS; y++) {
				Action action = newInstance(x, y);
				mapping.addUniqueAction(action);
			}
		}
	}

	private Action newInstance(int x, int y) {
		Action action = new Action(getType());
		action.addPayloadEntry(PAYLOAD_X, String.valueOf(x));
		action.addPayloadEntry(PAYLOAD_Y, String.valueOf(y));
		return action;
	}

	@Override
	public FeedbackType[] supportedFeedbackOptions(Action action, KeyType keyType) {
		switch (keyType) {
			case KEYBOARD:
				return new FeedbackType[0];
			case MIDI:
				return new FeedbackType[]{FeedbackType.DEFAULT};
		}
		return new FeedbackType[0];
	}

	@Override
	public ActionType getActionType() {
		return ActionType.CONTROL;
	}

	@Override
	public TreeItem<ActionSettingsEntry> getTreeItemForActions(List<Action> actions, Mapping mapping) {
		return new TreeItem<>(new PlaylistNextActionSettingsEntry());
	}
}
