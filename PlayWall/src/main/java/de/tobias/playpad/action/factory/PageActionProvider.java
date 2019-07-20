package de.tobias.playpad.action.factory;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.action.ActionHandler;
import de.tobias.playpad.action.ActionProvider;
import de.tobias.playpad.action.ActionType;
import de.tobias.playpad.action.actions.PageAction;
import de.tobias.playpad.action.settings.ActionSettingsEntry;
import de.tobias.playpad.action.settings.PageActionSettingsEntry;
import de.tobias.playpad.action.settings.PageActionTypeSettingsEntry;
import de.tobias.playpad.project.ProjectSettings;
import javafx.scene.control.TreeItem;

import java.util.List;

import static de.tobias.playpad.action.actions.PageAction.TYPE;

public class PageActionProvider extends ActionProvider {

	public PageActionProvider(String type) {
		super(TYPE);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void createDefaultActions(Mapping mapping) {
		for (int i = 0; i < ProjectSettings.MAX_PAGES; i++) {
			Action action = newInstance(i);
			mapping.addUniqueAction(action);
		}
	}

	private Action newInstance(int i) {
		Action action = new Action(getType());
		action.addPayloadEntry(PageAction.PAYLOAD_PAGE, String.valueOf(i));
		return action;
	}

	@Override
	public ActionHandler getActionHandler() {
		return new PageAction();
	}

	/*

	 */

	@Override
	public ActionType getActionType() {
		return ActionType.CONTROL;
	}

	@Override
	public TreeItem<ActionSettingsEntry> getTreeItemForActions(List<Action> actions, Mapping mapping) {
		TreeItem<ActionSettingsEntry> rootItem = new TreeItem<>(new PageActionTypeSettingsEntry());

		actions.sort((o1, o2) ->
		{
			int page1 = PageAction.getPageForAction(o1);
			int page2 = PageAction.getPageForAction(o2);
			return Long.compare(page1, page2);
		});

		for (Action action : actions) {
			TreeItem<ActionSettingsEntry> actionItem = new TreeItem<>(new PageActionSettingsEntry(action));
			rootItem.getChildren().add(actionItem);
		}

		return rootItem;
	}
}
