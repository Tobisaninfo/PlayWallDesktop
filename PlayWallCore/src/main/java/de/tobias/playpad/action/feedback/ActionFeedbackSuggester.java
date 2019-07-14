package de.tobias.playpad.action.feedback;

import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.feedback.FeedbackType;
import de.tobias.playpad.pad.Pad;

/**
 * Eine Action implementiert dieses Interface, falls die Feedbackfarbe automatisch an die Farbe der Kachel angepasst werden soll.
 *
 * @author tobias
 * @since 5.0.0
 */
public interface ActionFeedbackSuggester {

	/**
	 * Ist dieses Feature ative.
	 *
	 * @return <code>true</code> Active
	 */
	boolean isAutoFeedbackColors(Action action);

	void suggestFeedback(Action action);

	byte suggestFeedbackChannel(FeedbackType type);

	Pad getPad(Action action);
}
