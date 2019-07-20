package de.tobias.playpad.action.feedback;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.action.ActionHandler;
import de.thecodelabs.midi.action.ActionRegistry;
import de.thecodelabs.midi.feedback.Feedback;
import de.thecodelabs.midi.feedback.FeedbackColor;
import de.thecodelabs.midi.feedback.FeedbackType;
import de.thecodelabs.midi.feedback.FeedbackValue;
import de.thecodelabs.midi.mapping.MidiKey;
import de.thecodelabs.midi.midi.Midi;
import de.thecodelabs.midi.midi.feedback.MidiFeedbackTranscript;
import de.tobias.playpad.design.FeedbackDesignColorSuggester;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.profile.Profile;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.List;
import java.util.Optional;

/**
 * Eine Klasse mit nützlichen Methoden um die Farben bei den Mappern anzupassen.
 *
 * @author tobias
 * @since 5.1.0
 */
public class ColorAdjuster {

	/**
	 * Übernimmt die Farben des Pads und den verknüpften Aktionen zu einem Pad auf die Mapper.
	 */
	public static void applyColorsToKeys() {
		// Apply Layout to Mapper
		final Optional<Mapping> activeMapping = Profile.currentProfile().getMappings().getActiveMapping();
		activeMapping.ifPresent(mapping -> {
			final List<Action> actions = mapping.getActions();
			for (Action action : actions) {

				final ActionHandler actionHandler = ActionRegistry.getActionHandler(action.getActionType());

				if (actionHandler instanceof ActionFeedbackSuggester) {
					ActionFeedbackSuggester adjustable = (ActionFeedbackSuggester) actionHandler;

					if (adjustable.isAutoFeedbackColors(action)) {
						adjustable.suggestFeedback(action);
					}
				}
			}
		});
	}

	// COMMENT ColorAdjuster

	public static void setSuggestedFeedbackColors(ActionFeedbackSuggester suggester, Action action, MidiKey key) {
		MidiFeedbackTranscript transcript = Midi.getInstance().getFeedbackTranscript();
		if (transcript == null) {
			return;
		}

		Pad pad = suggester.getPad(action);

		Color layoutStdColor = null;
		Color layoutEvColor = null;

		FeedbackDesignColorSuggester design;
		if (pad.getPadSettings().isCustomDesign()) {
			design = pad.getPadSettings().getDesign();
		} else {
			design = Profile.currentProfile().getProfileSettings().getDesign();
		}

		if (design != null) {
			layoutStdColor = design.getDesignDefaultColor();
			layoutEvColor = design.getDesignEventColor();
		}

		if (layoutStdColor != null) {
			FeedbackColor matchedColor = searchColor(transcript, layoutStdColor);
			final byte channel = suggester.suggestFeedbackChannel(FeedbackType.DEFAULT);
			key.setDefaultFeedback(new Feedback(channel, matchedColor.getValue()));
		}

		if (layoutEvColor != null) {
			FeedbackColor matchedColor = searchColor(transcript, layoutEvColor);
			final byte channel = suggester.suggestFeedbackChannel(FeedbackType.EVENT);
			key.setEventFeedback(new Feedback(channel, matchedColor.getValue()));
		}

		if (layoutEvColor != null) {
			FeedbackColor matchedColor = searchColor(transcript, layoutEvColor);
			final byte channel = suggester.suggestFeedbackChannel(FeedbackType.WARNING);
			key.setWarningFeedback(new Feedback(channel, matchedColor.getValue()));
		}
	}

	/**
	 * Get a appropriate feedback color based on a given device and ui color.
	 *
	 * @param transcript device implementation
	 * @param color      ui color
	 * @return feedback color
	 */
	protected static FeedbackColor searchColor(MidiFeedbackTranscript transcript, Color color) {
		if (transcript instanceof FeedbackColorSuggester) {
			FeedbackColorSuggester suggester = (FeedbackColorSuggester) transcript;

			final FeedbackColor suggest = suggester.suggest(color);
			if (suggest != null) {
				return suggest;
			}
		}

		FeedbackColor minColor = null;
		double minVal = 1;

		for (FeedbackValue feedbackValue : transcript.getFeedbackValues()) {
			if (feedbackValue instanceof FeedbackColor) {
				Paint paint = ((FeedbackColor) feedbackValue).getColor();
				if (paint instanceof Color) {
					Color c = (Color) paint;
					double diff = Math.sqrt(Math.pow(c.getRed() - color.getRed(), 2) + Math.pow(c.getGreen() - color.getGreen(), 2)
							+ Math.pow(c.getBlue() - color.getBlue(), 2));
					if (minVal > diff) {
						minVal = diff;
						minColor = (FeedbackColor) feedbackValue;
					}
				}
			}
		}
		if (minColor != null && minVal < 0.35) {
			return minColor;
		} else {
			return null;
		}
	}
}
