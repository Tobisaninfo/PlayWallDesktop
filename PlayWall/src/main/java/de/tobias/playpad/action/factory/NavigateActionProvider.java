package de.tobias.playpad.action.factory;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.action.ActionHandler;
import de.thecodelabs.midi.feedback.FeedbackType;
import de.thecodelabs.midi.mapping.KeyType;
import de.tobias.playpad.action.ActionProvider;
import de.tobias.playpad.action.ActionType;
import de.tobias.playpad.action.actions.NavigateAction;
import de.tobias.playpad.action.actions.NavigateAction.NavigationType;
import de.tobias.playpad.action.settings.ActionSettingsEntry;
import de.tobias.playpad.action.settings.NavigateActionSettingsEntry;
import de.tobias.playpad.action.settings.NavigateActionTypeSettingsEntry;
import javafx.scene.control.TreeItem;

import java.util.List;

import static de.tobias.playpad.action.actions.NavigateAction.TYPE;

public class NavigateActionProvider extends ActionProvider {

	public NavigateActionProvider(String type) {
		super(TYPE);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void createDefaultActions(Mapping mapping) {
		mapping.addUniqueAction(newInstance(NavigationType.PREVIOUS));
		mapping.addUniqueAction(newInstance(NavigationType.NEXT));
	}

	private Action newInstance(NavigationType navigationType) {
		Action action = new Action(getType());
		action.addPayloadEntry(NavigateAction.PAYLOAD_TYPE, navigationType.name());
		return action;
	}

	@Override
	public ActionHandler getActionHandler() {
		return new NavigateAction();
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

	/*

	 */

	@Override
	public ActionType getActionType() {
		return ActionType.CONTROL;
	}

	@Override
	public TreeItem<ActionSettingsEntry> getTreeItemForActions(List<Action> actions, Mapping mapping) {
		TreeItem<ActionSettingsEntry> rootItem = new TreeItem<>(new NavigateActionTypeSettingsEntry());

		actions.sort((o1, o2) ->
		{
			final NavigationType value1 = NavigationType.valueOf(o1);
			final NavigationType value2 = NavigationType.valueOf(o2);
			return Long.compare(value1.ordinal(), value2.ordinal());
		});

		for (Action action : actions) {
			TreeItem<ActionSettingsEntry> actionItem = new TreeItem<>(new NavigateActionSettingsEntry(action));
			rootItem.getChildren().add(actionItem);
		}
		return rootItem;
	}
}
