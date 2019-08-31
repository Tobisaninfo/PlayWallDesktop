package de.tobias.playpad.action.actions;

import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.action.ActionHandler;
import de.thecodelabs.midi.event.KeyEvent;
import de.thecodelabs.midi.event.KeyEventType;
import de.thecodelabs.midi.feedback.FeedbackType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.application.Platform;

public class NavigateAction extends ActionHandler {

	public static final String TYPE = "NavigateAction";
	public static final String PAYLOAD_TYPE = "type";

	public enum NavigationType {
		PREVIOUS,
		NEXT;

		public static NavigationType valueOf(Action action) {
			return valueOf(action.getPayload(PAYLOAD_TYPE));
		}

		@Override
		public String toString() {
			return Localization.getString(Strings.NAVIGATION_TYPE + name());
		}
	}

	@Override
	public String actionType() {
		return TYPE;
	}

	@Override
	public FeedbackType handle(KeyEvent keyEvent, Action action) {
		if (keyEvent.getKeyEventType() == KeyEventType.DOWN) {
			IMainViewController mainViewController = PlayPadPlugin.getInstance().getMainViewController();
			switch (NavigationType.valueOf(action)) {
				case PREVIOUS:
					Platform.runLater(() -> mainViewController.showPage(mainViewController.getPage() - 1));
					break;
				case NEXT:
					Platform.runLater(() -> mainViewController.showPage(mainViewController.getPage() + 1));
					break;
				default:
					break;
			}
		}
		return FeedbackType.DEFAULT;
	}

	@Override
	public FeedbackType getCurrentFeedbackType(Action action) {
		return FeedbackType.DEFAULT;
	}
}
