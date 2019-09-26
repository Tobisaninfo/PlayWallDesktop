package de.tobias.playpad.action.feedback;

import de.thecodelabs.midi.feedback.FeedbackColor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum LightMode {
	LOW,
	MIDDLE,
	NORMAL,
	HIGH;

	public interface ILightMode extends FeedbackColor {
		LightMode getLightMode();

		FeedbackColor translate(LightMode lightMode);
	}

	public static List<ILightMode> filter(ILightMode[] values, LightMode filter) {
		return Stream.of(values)
				.filter(v -> v.getLightMode() == filter)
				.collect(Collectors.toList());
	}
}