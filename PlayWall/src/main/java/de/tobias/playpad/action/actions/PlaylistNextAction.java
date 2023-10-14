package de.tobias.playpad.action.actions;

import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.action.ActionHandler;
import de.thecodelabs.midi.event.KeyEvent;
import de.thecodelabs.midi.feedback.FeedbackType;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.Playlistable;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.main.IMainViewController;

public class PlaylistNextAction extends ActionHandler {
	public static final String TYPE = "PlaylistNextAction";
	public static final String PAYLOAD_X = "x";
	public static final String PAYLOAD_Y = "y";

	@Override
	public String actionType() {
		return TYPE;
	}

	@Override
	public FeedbackType handle(KeyEvent keyEvent, Action action) {
		final Pad pad = getPad(action);
		if (pad == null) {
			return FeedbackType.NONE;
		}

		if (pad.hasVisibleContent() && pad.getContent() instanceof Playlistable) {
			((Playlistable) pad.getContent()).next();
		}
		return getCurrentFeedbackType(action);
	}

	@Override
	public FeedbackType getCurrentFeedbackType(Action action) {
		Project project = PlayPadPlugin.getInstance().getCurrentProject();
		IMainViewController mainViewController = PlayPadPlugin.getInstance().getMainViewController();

		Pad pad = project.getPad(getX(action), getY(action), mainViewController.getPage());

		if (pad == null || !(pad.getContent() instanceof Playlistable)) {
			return FeedbackType.NONE;
		}
		return FeedbackType.DEFAULT;
	}

	public Pad getPad(Action action) {
		Project project = PlayPadPlugin.getInstance().getCurrentProject();
		IMainViewController mainViewController = PlayPadPlugin.getInstance().getMainViewController();

		int x = getX(action);
		int y = getY(action);
		final int page = mainViewController.getPage();

		return project.getPad(x, y, page);
	}

	/*
	Property accessors
	 */

	public static int getY(Action action) {
		return Integer.parseInt(action.getPayload(PAYLOAD_Y));
	}

	public static int getX(Action action) {
		return Integer.parseInt(action.getPayload(PAYLOAD_X));
	}

	public static void setX(Action action, int x) {
		action.addPayloadEntry(CartAction.PAYLOAD_X, String.valueOf(x));
	}

	public static void setY(Action action, int y) {
		action.addPayloadEntry(CartAction.PAYLOAD_Y, String.valueOf(y));
	}

}
