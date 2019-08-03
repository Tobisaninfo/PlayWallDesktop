package de.tobias.playpad.action.feedback;

import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.feedback.FeedbackType;
import de.tobias.playpad.pad.Pad;

/**
 * The interface provides methods for handling automatic feedback color choosing for an action.
 *
 * @author tobias
 * @since 5.0.0
 */
public interface ActionFeedbackSuggester {

	/**
	 * Is automatic feedback suggestion enabled
	 *
	 * @return <code>true</code> Active
	 */
	boolean isAutoFeedbackColors(Action action);

	void suggestFeedback(Action action);

	byte suggestFeedbackChannel(FeedbackType type);

	Pad getPad(Action action);
}
