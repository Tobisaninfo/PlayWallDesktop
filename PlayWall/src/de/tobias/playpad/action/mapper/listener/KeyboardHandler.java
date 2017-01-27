package de.tobias.playpad.action.mapper.listener;

import java.util.List;

import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.InputType;
import de.tobias.playpad.action.MappingUtils;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

public class KeyboardHandler implements EventHandler<KeyEvent> {

	private Project project;
	private IMainViewController mainViewController;

	public KeyboardHandler(Project project, IMainViewController mainViewController) {
		this.project = project;
		this.mainViewController = mainViewController;

		mainViewController.registerKeyboardListener(KeyEvent.ANY, this);
	}

	// KeyType ist nicht unterstützt.
	@Override
	public void handle(KeyEvent event) {
		if (event.getTarget() == mainViewController.getParent()) {
			if (!event.isShortcutDown()) {
				KeyCode code = null;
				InputType type = null;

				if (event.getEventType() == KeyEvent.KEY_PRESSED) {
					code = event.getCode();
					type = InputType.PRESSED;

				} else if (event.getEventType() == KeyEvent.KEY_RELEASED) {
					code = event.getCode();
					type = InputType.RELEASED;

				}

				// Only execute this, then the right event is triggered and this var is set
				if (code != null) {
					List<Action> actions = MappingUtils.getActionsForKey(code, Profile.currentProfile().getMappings().getActiveMapping());

					executeActions(type, actions);
				}
			}
		}
	}

	private void executeActions(InputType type, List<Action> actions) {
		for (Action action : actions) {
			try {
				action.performAction(type, project, mainViewController);
			} catch (Exception e) {
				e.printStackTrace();
				// TODO Exception Hadling
			}
		}
	}

	public void setProject(Project project) {
		this.project = project;
	}
}
