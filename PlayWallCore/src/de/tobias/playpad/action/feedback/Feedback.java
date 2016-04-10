package de.tobias.playpad.action.feedback;

import org.dom4j.Element;

public abstract class Feedback {

	public abstract int getValueForFeedbackMessage(FeedbackMessage message);

	public abstract void setFeedback(FeedbackMessage feedbackMessage, int value);

	public abstract void load(Element root);

	public abstract void save(Element root);

	public abstract Feedback cloneFeedback() throws CloneNotSupportedException ;

}
