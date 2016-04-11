package de.tobias.playpad.action.mapper.listener;

import java.util.List;

import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.InputType;
import de.tobias.playpad.action.MappingUtils;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class KeyboardHandler implements EventHandler<KeyEvent> {

	private Project project;
	private IMainViewController mainViewController;

	public KeyboardHandler(Project project, IMainViewController mainViewController) {
		this.project = project;
		this.mainViewController = mainViewController;

		mainViewController.getParent().getScene().setOnKeyPressed(this);
		mainViewController.getParent().getScene().setOnKeyReleased(this);
		mainViewController.getParent().getScene().setOnKeyTyped(this);
	}

	private boolean[] keys = new boolean[KeyCode.values().length];

	@Override
	public void handle(KeyEvent event) {
		if (!event.isShortcutDown()) {
			KeyCode code = null;
			InputType type = null;

			if (event.getEventType() == KeyEvent.KEY_PRESSED) {
				code = event.getCode();
				type = InputType.PRESSED;

				if (keys[code.ordinal()] == true) {
					return;
				}

				keys[code.ordinal()] = true;
			} else if (event.getEventType() == KeyEvent.KEY_RELEASED) {
				code = event.getCode();
				type = InputType.RELEASED;

				keys[code.ordinal()] = false;
			}

			// Only execute this, then the right event is triggered and this var is set
			if (code != null) {
				List<Action> actions = MappingUtils.getActionsForKey(code, Profile.currentProfile().getMappings().getActiveMapping());

				for (Action action : actions) {
					action.performAction(type, project, mainViewController);
				}
			}
		}
	}

	public void setProject(Project project) {
		this.project = project;
	}
}
