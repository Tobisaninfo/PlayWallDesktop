package de.tobias.playpad.action.feedback;

public interface ColorAssociator {

	public DisplayableFeedbackColor[] getColors();

	public DisplayableFeedbackColor getDefaultStandardColor();

	public DisplayableFeedbackColor getDefaultEventColor();

	public void setColor(FeedbackMessage feedbackMessage, int value);
}
