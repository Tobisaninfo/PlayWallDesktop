package de.tobias.playpad.action.mapper.feedback;

import org.dom4j.Element;

import de.tobias.playpad.action.feedback.Feedback;
import de.tobias.playpad.action.feedback.FeedbackMessage;

/**
 * Implementierung eines 1 State Feedbacks für MIDI Geräte.
 * 
 * @author tobias
 * 
 * @since 5.0.0
 *
 */
public class SingleMidiFeedback extends Feedback {

	private static final int INIT_FEEDBACK_VALUE = 0;

	private int feedbackValue; // e.g. Color on an LaunchPad

	public SingleMidiFeedback() {
		this.feedbackValue = INIT_FEEDBACK_VALUE;
	}

	public SingleMidiFeedback(int feedbackValue) {
		this.feedbackValue = feedbackValue;
	}

	public int getFeedbackValue() {
		return feedbackValue;
	}

	public void setFeedbackValue(int feedbackValue) {
		this.feedbackValue = feedbackValue;
	}

	@Override
	public int getValueForFeedbackMessage(FeedbackMessage message) {
		switch (message) {
		case EVENT:
			return feedbackValue;
		case OFF:
			return 0;
		case STANDARD:
			return feedbackValue;
		default:
			break;
		}
		return 0;
	}

	@Override
	public void setFeedback(FeedbackMessage message, int value) {
		if (message == FeedbackMessage.STANDARD) {
			feedbackValue = value;
		}
	}

	private static final String FEEDBACK_VALUE = "default";

	@Override
	public void load(Element root) {
		String value = root.attributeValue(FEEDBACK_VALUE);
		if (value != null) {
			this.feedbackValue = Integer.valueOf(value);
		}
	}

	@Override
	public void save(Element root) {
		root.addAttribute(FEEDBACK_VALUE, String.valueOf(feedbackValue));
	}

	@Override
	public Feedback cloneFeedback() throws CloneNotSupportedException {
		SingleMidiFeedback feedback = (SingleMidiFeedback) super.clone();

		feedback.feedbackValue = feedbackValue;

		return feedback;
	}
}
