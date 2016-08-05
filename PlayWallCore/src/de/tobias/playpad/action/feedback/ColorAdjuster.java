package de.tobias.playpad.action.feedback;

import java.util.Set;

import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.action.mapper.MapperFeedbackable;
import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.DesignColorAssociator;
import de.tobias.playpad.design.GlobalDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import javafx.scene.paint.Color;

public class ColorAdjuster {

	public static void applyColorsToMappers(Project project) {
		// Apply Layout to Mapper
		Set<Action> actions = Profile.currentProfile().getMappings().getActiveMapping().getActions();
		for (Action action : actions) {
			if (action instanceof ColorAdjustable) {
				ColorAdjustable adjustable = (ColorAdjustable) action;
				if (adjustable.isAutoFeedbackColors()) {
					for (Mapper mapper : action.getMappers()) {
						if (mapper instanceof MapperFeedbackable) {
							mapColorForMapper(adjustable, mapper, project);
						}
					}
				}
			}
		}
	}

	private static void mapColorForMapper(ColorAdjustable cartAction, Mapper mapper, Project project) {
		MapperFeedbackable feedbackable = (MapperFeedbackable) mapper;
		if (feedbackable.supportFeedback() && mapper instanceof ColorAssociator) {
			ColorAssociator colorAssociator = (ColorAssociator) mapper;

			Pad pad = project.getPad(cartAction.getCart());
			Color layoutStdColor = null;
			Color layoutEvColor = null;

			if (pad.isCustomLayout()) {
				CartDesign layout = pad.getLayout();
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
				DisplayableFeedbackColor associator = Mapper.searchColor(colorAssociator, FeedbackMessage.STANDARD, layoutStdColor);
				colorAssociator.setColor(FeedbackMessage.STANDARD, associator.midiVelocity());
			}

			if (layoutEvColor != null) {
				DisplayableFeedbackColor associator = Mapper.searchColor(colorAssociator, FeedbackMessage.EVENT, layoutEvColor);
				colorAssociator.setColor(FeedbackMessage.EVENT, associator.midiVelocity());
			}
		}
	}
}
