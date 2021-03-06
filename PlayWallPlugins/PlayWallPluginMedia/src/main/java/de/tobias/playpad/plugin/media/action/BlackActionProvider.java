package de.tobias.playpad.plugin.media.action;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.action.ActionHandler;
import de.thecodelabs.midi.feedback.FeedbackType;
import de.thecodelabs.midi.mapping.KeyType;
import de.tobias.playpad.action.ActionProvider;
import de.tobias.playpad.action.ActionType;
import de.tobias.playpad.action.settings.ActionSettingsEntry;
import de.tobias.playpad.plugin.media.action.settings.BlackActionTypeSettingsEntry;
import javafx.scene.control.TreeItem;

import java.util.List;

import static de.tobias.playpad.plugin.media.action.BlackAction.TYPE;

public class BlackActionProvider extends ActionProvider {

	public BlackActionProvider(String type) {
		super(TYPE);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void createDefaultActions(Mapping mapping) {
		mapping.addUniqueAction(new Action(getType()));
	}

	@Override
	public ActionHandler getActionHandler() {
		return new BlackAction();
	}

	@Override
	public FeedbackType[] supportedFeedbackOptions(Action action, KeyType keyType) {
		switch (keyType) {
			case KEYBOARD:
				return new FeedbackType[0];
			case MIDI:
				return new FeedbackType[]{FeedbackType.DEFAULT, FeedbackType.EVENT};
		}
		return new FeedbackType[0];
	}

	/*

	 */

	@Override
	public ActionType getActionType() {
		return ActionType.SETTINGS;
	}

	@Override
	public TreeItem<ActionSettingsEntry> getTreeItemForActions(List<Action> actions, Mapping mapping) {
		return new TreeItem<>(new BlackActionTypeSettingsEntry(actions.stream().findFirst().orElse(null)));
	}
}
