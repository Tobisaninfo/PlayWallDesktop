package de.tobias.playpad.action.actions;

import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.action.ActionHandler;
import de.thecodelabs.midi.event.KeyEvent;
import de.thecodelabs.midi.feedback.FeedbackType;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.project.Project;

public class StopAction extends ActionHandler {

	public static final String TYPE = "StopAction";

	@Override
	public String actionType() {
		return TYPE;
	}

	@Override
	public FeedbackType handle(KeyEvent keyEvent, Action action) {
		final Project project = PlayPadPlugin.getInstance().getCurrentProject();

		for (Pad pad : project.getPads()) {
			if (pad.getStatus() == PadStatus.PLAY || pad.getStatus() == PadStatus.PAUSE)
				pad.setStatus(PadStatus.STOP, true);
		}

		return FeedbackType.DEFAULT;
	}

	@Override
	public FeedbackType getCurrentFeedbackType(Action action) {
		return FeedbackType.DEFAULT;
	}
}
