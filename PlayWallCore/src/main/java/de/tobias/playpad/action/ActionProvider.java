package de.tobias.playpad.action;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.action.ActionHandler;
import de.thecodelabs.midi.feedback.FeedbackType;
import de.thecodelabs.midi.mapping.KeyType;
import de.tobias.playpad.action.settings.ActionSettingsEntry;
import de.tobias.playpad.registry.Component;
import javafx.scene.control.TreeItem;

import java.util.List;

public abstract class ActionProvider extends Component {

	public ActionProvider(String type) {
		super(type);
	}

	public abstract String getType();

	public abstract ActionHandler getActionHandler();

	public abstract void createDefaultActions(Mapping mapping);

	public abstract FeedbackType[] supportedFeedbackOptions(Action action, KeyType keyType);

	// Settings

	public abstract ActionType getActionType();

	public abstract TreeItem<ActionSettingsEntry> getTreeItemForActions(List<Action> actions, Mapping mapping);
}
