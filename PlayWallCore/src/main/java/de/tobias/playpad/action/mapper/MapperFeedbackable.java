package de.tobias.playpad.action.mapper;

import de.tobias.playpad.action.feedback.FeedbackMessage;

/**
 * Implement this interface to enable a mapper to receive feedback from an action
 *
 * @author tobias
 */
public interface MapperFeedbackable {

	boolean supportFeedback();

	void handleFeedback(FeedbackMessage message);

}