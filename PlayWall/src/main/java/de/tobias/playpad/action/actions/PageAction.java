package de.tobias.playpad.action.actions;

import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.action.ActionHandler;
import de.thecodelabs.midi.event.KeyEvent;
import de.thecodelabs.midi.feedback.FeedbackType;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.application.Platform;

public class PageAction extends ActionHandler {

	public static final String TYPE = "PageAction";
	public static final String PAYLOAD_PAGE = "page";

	@Override
	public String actionType() {
		return TYPE;
	}

	@Override
	public FeedbackType handle(KeyEvent keyEvent, Action action) {
		Project project = PlayPadPlugin.getInstance().getCurrentProject();
		IMainViewController mainViewController = PlayPadPlugin.getInstance().getMainViewController();
		int targetPage = getPageForAction(action);

		if (targetPage < 0 || targetPage >= project.getPages().size()) {
			return FeedbackType.DEFAULT;
		}

		Platform.runLater(() -> mainViewController.showPage(targetPage));

		return FeedbackType.EVENT;
	}

	@Override
	public FeedbackType getCurrentFeedbackType(Action action) {
		IMainViewController mainViewController = PlayPadPlugin.getInstance().getMainViewController();
		int targetPage = getPageForAction(action);

		if (mainViewController.getPage() == targetPage) {
			return FeedbackType.EVENT;
		}

		return FeedbackType.DEFAULT;
	}

	public static int getPageForAction(Action action) {
		return Integer.parseInt(action.getPayload(PAYLOAD_PAGE));
	}
}
