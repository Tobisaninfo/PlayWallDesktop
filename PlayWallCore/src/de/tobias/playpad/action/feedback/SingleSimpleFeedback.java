package de.tobias.playpad.action.feedback;

import org.dom4j.Element;

public class SingleSimpleFeedback extends Feedback {

	private static final int INIT_FEEDBACK_VALUE = 0;

	private int feedbackValue; // e.g. Color on an LaunchPad

	public SingleSimpleFeedback() {
		this.feedbackValue = INIT_FEEDBACK_VALUE;
	}

	public SingleSimpleFeedback(int feedbackValue) {
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
		SingleSimpleFeedback feedback = (SingleSimpleFeedback) super.clone();

		feedback.feedbackValue = feedbackValue;

		return feedback;
	}
}
