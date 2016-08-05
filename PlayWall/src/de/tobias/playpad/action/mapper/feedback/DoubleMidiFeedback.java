package de.tobias.playpad.action.mapper.feedback;

import org.dom4j.Element;

import de.tobias.playpad.action.feedback.Feedback;
import de.tobias.playpad.action.feedback.FeedbackMessage;

/**
 * Implementierung eines 2 State Feedbacks für MIDI Geräte.
 * 
 * @author tobias
 * 
 * @since 5.0.0
 *
 */
public class DoubleMidiFeedback extends Feedback {

	private static final int INIT_FEEDBACK_VALUE = 0;

	private int feedbackDefaultValue; // e.g. Color on an LaunchPad
	private int feedbackEventValue; // e.g. Color on an LaunchPad

	public DoubleMidiFeedback() {
		this.feedbackDefaultValue = INIT_FEEDBACK_VALUE;
		this.feedbackEventValue = INIT_FEEDBACK_VALUE;
	}

	public DoubleMidiFeedback(int feedbackDefaultValue, int feedbackEventValue) {
		this.feedbackDefaultValue = feedbackDefaultValue;
		this.feedbackEventValue = feedbackEventValue;
	}

	public int getFeedbackDefaultValue() {
		return feedbackDefaultValue;
	}

	public int getFeedbackEventValue() {
		return feedbackEventValue;
	}

	public void setFeedbackDefaultValue(int feedbackDefaultValue) {
		this.feedbackDefaultValue = feedbackDefaultValue;
	}

	public void setFeedbackEventValue(int feedbackEventValue) {
		this.feedbackEventValue = feedbackEventValue;
	}

	@Override
	public int getValueForFeedbackMessage(FeedbackMessage message) {
		if (message == FeedbackMessage.EVENT) {
			return feedbackEventValue;
		} else if (message == FeedbackMessage.OFF) {
			return 0;
		} else if (message == FeedbackMessage.STANDARD) {
			return feedbackDefaultValue;
		} else {
			return 0;
		}
	}

	@Override
	public void setFeedback(FeedbackMessage message, int value) {
		if (message == FeedbackMessage.EVENT) {
			feedbackEventValue = value;
		} else if (message == FeedbackMessage.STANDARD) {
			feedbackDefaultValue = value;
		}
	}

	private static final String FEEDBACK_DEFAULT_VALUE = "default";
	private static final String FEEDBACK_EVENT_VALUE = "event";

	@Override
	public void load(Element root) {
		String defaultValue = root.attributeValue(FEEDBACK_DEFAULT_VALUE);
		if (defaultValue != null) {
			this.feedbackDefaultValue = Integer.valueOf(defaultValue);
		}
		String eventValue = root.attributeValue(FEEDBACK_EVENT_VALUE);
		if (eventValue != null) {
			this.feedbackEventValue = Integer.valueOf(eventValue);
		}
	}

	@Override
	public void save(Element root) {
		root.addAttribute(FEEDBACK_DEFAULT_VALUE, String.valueOf(feedbackDefaultValue));
		root.addAttribute(FEEDBACK_EVENT_VALUE, String.valueOf(feedbackEventValue));
	}

	@Override
	public Feedback cloneFeedback() throws CloneNotSupportedException {
		DoubleMidiFeedback feedback = (DoubleMidiFeedback) super.clone();

		feedback.feedbackDefaultValue = feedbackDefaultValue;
		feedback.feedbackEventValue = feedbackEventValue;

		return feedback;
	}

}
