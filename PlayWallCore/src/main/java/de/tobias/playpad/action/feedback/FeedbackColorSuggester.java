package de.tobias.playpad.action.feedback;

import de.thecodelabs.midi.feedback.FeedbackColor;
import javafx.scene.paint.Color;

public interface FeedbackColorSuggester {

	FeedbackColor suggest(Color color);
}
