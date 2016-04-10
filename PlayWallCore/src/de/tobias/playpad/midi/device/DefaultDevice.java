package de.tobias.playpad.midi.device;

import de.tobias.playpad.action.feedback.DisplayableFeedbackColor;
import de.tobias.playpad.action.feedback.Feedback;
import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.action.mididevice.Device;

public class DefaultDevice extends Device {

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean supportFeedback() {
		return false;
	}

	@Override
	public void handleFeedback(FeedbackMessage type, int key, Feedback feedback) {}

	@Override
	public void initFeedback() {}

	@Override
	public void clearFeedback() {}

	@Override
	public DisplayableFeedbackColor getColor(int id) {
		return null;
	}

	@Override
	public DisplayableFeedbackColor[] getColors() {
		return null;
	}
}
