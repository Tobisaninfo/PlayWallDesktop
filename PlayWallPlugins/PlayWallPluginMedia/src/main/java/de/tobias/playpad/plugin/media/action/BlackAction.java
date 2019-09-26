package de.tobias.playpad.plugin.media.action;

import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.action.ActionHandler;
import de.thecodelabs.midi.event.KeyEvent;
import de.thecodelabs.midi.feedback.FeedbackType;
import de.tobias.playpad.plugin.media.main.impl.MediaPluginImpl;

public class BlackAction extends ActionHandler {

	public static final String TYPE = "BLACK";

	@Override
	public String actionType() {
		return TYPE;
	}

	@Override
	public FeedbackType handle(KeyEvent keyEvent, Action action) {
		MediaPluginImpl.blindProperty().set(!MediaPluginImpl.blindProperty().get());
		return getCurrentFeedbackType(action);
	}

	@Override
	public FeedbackType getCurrentFeedbackType(Action action) {
		return MediaPluginImpl.blindProperty().getValue() ? FeedbackType.EVENT : FeedbackType.DEFAULT;
	}
}
