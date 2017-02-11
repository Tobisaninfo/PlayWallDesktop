package de.tobias.playpad.action.feedback;

/**
 * Arten von Feedback Meldungen.
 * 
 * @author tobias
 *
 * @since 5.0.0
 * 
 * @see Feedback#getValueForFeedbackMessage(FeedbackMessage)
 */
public enum FeedbackMessage {

	/**
	 * Feedback Aus.
	 */
	OFF,
	/**
	 * Standart bei keiner Aktion.
	 */
	STANDARD,
	/**
	 * Besondere Aktion.
	 */
	EVENT,
	/**
	 * Wichtiger Hinweis auf dem Mapper.
	 */
	WARNING
}
