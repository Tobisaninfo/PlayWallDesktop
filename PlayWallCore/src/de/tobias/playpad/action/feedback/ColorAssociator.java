package de.tobias.playpad.action.feedback;

import javafx.scene.paint.Color;

public interface ColorAssociator {

	public DisplayableFeedbackColor[] getColors();

	public DisplayableFeedbackColor getDefaultStandardColor();

	public DisplayableFeedbackColor getDefaultEventColor();

	public void setColor(FeedbackMessage feedbackMessage, int value);

	public DisplayableFeedbackColor map(Color color);
}
