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
import de.tobias.playpad.design.modern.model.ModernCartDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.profile.Profile;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import java.util.List;
import java.util.Optional;

/**
 * This class provides methods for calculating the best matched feedback color
 *
 * @author tobias
 * @since 5.1.0
 */
public class ColorAdjuster {

	private ColorAdjuster() {
	}

	/**
	 * Suggest for all actions in the current mapping feedback values
	 */
	public static void applyColorsToKeys() {
		// Apply Layout to Mapper
		final Optional<Mapping> activeMapping = Profile.currentProfile().getMappings().getActiveMapping();
		activeMapping.ifPresent(mapping -> {
			final List<Action> actions = mapping.getActions();
			for (Action action : actions) {

				final ActionHandler actionHandler = ActionRegistry.getActionHandler(action.getActionType());

				if (actionHandler instanceof ActionFeedbackSuggester) {
					ActionFeedbackSuggester suggester = (ActionFeedbackSuggester) actionHandler;

					if (suggester.isAutoFeedbackColors(action)) {
						suggester.suggestFeedback(action);
					}
				}
			}
		});
	}

	/**
	 * Suggest the feedback for a midi key depending on an action.
	 *
	 * @param suggester feedback suggester
	 * @param action    action
	 * @param key       midi key
	 */
	public static void setSuggestedFeedbackColors(ActionFeedbackSuggester suggester, Action action, MidiKey key) {
		MidiFeedbackTranscript transcript = Midi.getInstance().getFeedbackTranscript();
		if (transcript == null) {
			return;
		}

		Pad pad = suggester.getPad(action);

		FeedbackDesignColorSuggester globalDesign = Profile.currentProfile().getProfileSettings().getDesign();
		Color layoutStdColor = globalDesign.getDesignDefaultColor();
		Color layoutEvColor = globalDesign.getDesignEventColor();

		if(pad != null)
		{
			final ModernCartDesign padDesign = pad.getPadSettings().getDesign();

			if(padDesign.isEnableCustomBackgroundColor())
			{
				layoutStdColor = padDesign.getDesignDefaultColor();
			}

			if(padDesign.isEnableCustomPlayColor())
			{
				layoutEvColor = padDesign.getDesignEventColor();
			}
		}

		if (layoutStdColor != null) {
			searchColor(transcript, layoutStdColor).ifPresent(matchedColor -> {
				final byte channel = suggester.suggestFeedbackChannel(FeedbackType.DEFAULT);
				key.setDefaultFeedback(new Feedback(channel, matchedColor.getValue()));
			});
		}

		if (layoutEvColor != null) {
			searchColor(transcript, layoutEvColor).ifPresent(matchedColor -> {
				final byte channel = suggester.suggestFeedbackChannel(FeedbackType.EVENT);
				key.setEventFeedback(new Feedback(channel, matchedColor.getValue()));
			});

			searchColor(transcript, layoutEvColor).ifPresent(matchedColor -> {
				final byte channel = suggester.suggestFeedbackChannel(FeedbackType.WARNING);
				key.setWarningFeedback(new Feedback(channel, matchedColor.getValue()));
			});
		}
	}

	/**
	 * Get a appropriate feedback color based on a given device and ui color.
	 *
	 * @param transcript device implementation
	 * @param color      ui color
	 * @return feedback color
	 */
	private static Optional<FeedbackValue> searchColor(MidiFeedbackTranscript transcript, Color color) {

		// Look for predefined colors
		if (transcript instanceof FeedbackColorSuggester) {
			FeedbackColorSuggester suggester = (FeedbackColorSuggester) transcript;

			final FeedbackColor suggest = suggester.suggest(color);
			if (suggest != null) {
				return Optional.of(suggest);
			}
		}

		// Calculate
		return calculateNearestColor(transcript, color);
	}

	private static Optional<FeedbackValue> calculateNearestColor(MidiFeedbackTranscript transcript, Color color) {
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
			return Optional.of(minColor);
		} else {
			return Optional.empty();
		}
	}
}
