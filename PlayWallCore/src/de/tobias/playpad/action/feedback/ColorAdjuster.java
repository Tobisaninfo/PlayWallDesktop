package de.tobias.playpad.action.feedback;

import java.util.Set;

import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.action.mapper.MapperFeedbackable;
import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.DesignColorAssociator;
import de.tobias.playpad.design.GlobalDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.profile.Profile;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Eine Klasse mit nützlichen Methoden um die Farben bei den Mappern anzupassen.
 * 
 * @author tobias
 * 
 * @since 5.1.0
 * 
 * @see ColorAdjustable Action muss dieses Interface dafür Implementieren, damit die Farbe Automatisch zum pad gemacht
 *      wird.
 * @see ColorAssociator Mapper muss dieses Interface implemetieren, damit die entsprechenden Farbe gefunden werden kann
 *
 */
public class ColorAdjuster {

	/**
	 * Übernimmt die Farben des Pads und den verknüpften Aktionen zu einem Pad auf die Mapper.
	 */
	public static void applyColorsToMappers() {
		// Apply Layout to Mapper
		Set<Action> actions = Profile.currentProfile().getMappings().getActiveMapping().getActions();
		for (Action action : actions) {
			if (action instanceof ColorAdjustable) {
				ColorAdjustable adjustable = (ColorAdjustable) action;
				if (adjustable.isAutoFeedbackColors()) {
					for (Mapper mapper : action.getMappers()) {
						if (mapper instanceof MapperFeedbackable) {
							mapColorForMapper(adjustable, mapper);
						}
					}
				}
			}
		}
	}

	// COMMENT ColorAdjuster

	private static void mapColorForMapper(ColorAdjustable cartAction, Mapper mapper) {
		MapperFeedbackable feedbackable = (MapperFeedbackable) mapper;
		if (feedbackable.supportFeedback() && mapper instanceof ColorAssociator) {
			ColorAssociator colorAssociator = (ColorAssociator) mapper;

			Pad pad = cartAction.getPad();
			Color layoutStdColor = null;
			Color layoutEvColor = null;

			if (pad.getPadSettings().isCustomDesign()) {
				CartDesign layout = pad.getPadSettings().getDesign();
				if (layout instanceof DesignColorAssociator) {
					DesignColorAssociator associator = (DesignColorAssociator) layout;
					layoutStdColor = associator.getAssociatedStandardColor();
					layoutEvColor = associator.getAssociatedEventColor();
				}
			} else {
				GlobalDesign layout = Profile.currentProfile().currentLayout();
				if (layout instanceof DesignColorAssociator) {
					DesignColorAssociator associator = (DesignColorAssociator) layout;
					layoutStdColor = associator.getAssociatedStandardColor();
					layoutEvColor = associator.getAssociatedEventColor();
				}
			}

			if (layoutStdColor != null) {
				DisplayableFeedbackColor matchedColor = searchColor(colorAssociator, FeedbackMessage.STANDARD, layoutStdColor);
				colorAssociator.setColor(FeedbackMessage.STANDARD, matchedColor);
			}

			if (layoutEvColor != null) {
				DisplayableFeedbackColor matchedColor = searchColor(colorAssociator, FeedbackMessage.EVENT, layoutEvColor);
				colorAssociator.setColor(FeedbackMessage.EVENT, matchedColor);
			}
		}
	}

	protected static DisplayableFeedbackColor searchColor(ColorAssociator colorAssociator, FeedbackMessage message, Color color) {
		DisplayableFeedbackColor minColor = colorAssociator.map(color);
		if (minColor != null) {
			return minColor;
		}
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
		if (minColor != null && minVal < 0.35) {
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
