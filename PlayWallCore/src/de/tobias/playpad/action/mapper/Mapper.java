package de.tobias.playpad.action.mapper;

import org.dom4j.Element;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.feedback.ColorAssociator;
import de.tobias.playpad.action.feedback.DisplayableFeedbackColor;
import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.action.feedback.FeedbackType;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public abstract class Mapper implements Displayable, Cloneable {

	protected FeedbackType feedbackType;

	public void setFeedbackType(FeedbackType feedbackType) {
		this.feedbackType = feedbackType;
		initFeedback();
	}

	public FeedbackType getFeedbackType() {
		return feedbackType;
	}

	public abstract String getType();

	protected abstract void initFeedback();

	public abstract void load(Element element, Action action);

	public abstract void save(Element element, Action action);

	public abstract Mapper cloneMapper() throws CloneNotSupportedException;

	public static DisplayableFeedbackColor searchColor(ColorAssociator colorAssociator, FeedbackMessage message, Color color) {
		DisplayableFeedbackColor minColor = null;
		double minVal = 1;

		for (DisplayableFeedbackColor feedbackColor : colorAssociator.getColors()) {
			Paint paint = feedbackColor.getPaint();
			if (paint instanceof Color) {
				Color c = (Color) paint;
				double diff = Math.sqrt(Math.pow(c.getRed() - color.getRed(), 2) + Math.pow(c.getGreen() - color.getGreen(), 2)
						+ Math.pow(c.getBlue() - color.getBlue(), 2));
				if (minVal > diff) {
					minVal = diff;
					minColor = feedbackColor;
				}
			}
		}
		if (minColor != null && minVal < 0.4) {
			return minColor;
		} else if (message == FeedbackMessage.STANDARD) {
			return colorAssociator.getDefaultStandardColor();
		} else if (message == FeedbackMessage.EVENT) {
			return colorAssociator.getDefaultEventColor();
		} else {
			return null;
		}
	}
}
