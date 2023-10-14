package de.tobias.playpad.action.feedback;

import de.thecodelabs.midi.feedback.FeedbackColor;
import javafx.scene.paint.Color;

import java.util.List;

public interface FeedbackColorSuggester {

	List<String> getMidiColorMappings();

	FeedbackColor suggest(Color color);
}
