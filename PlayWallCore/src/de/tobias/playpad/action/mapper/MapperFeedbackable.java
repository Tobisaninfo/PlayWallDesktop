package de.tobias.playpad.action.mapper;

import de.tobias.playpad.action.feedback.FeedbackMessage;

public interface MapperFeedbackable {

	public abstract boolean supportFeedback();

	public abstract void handleFeedback(FeedbackMessage type);

}