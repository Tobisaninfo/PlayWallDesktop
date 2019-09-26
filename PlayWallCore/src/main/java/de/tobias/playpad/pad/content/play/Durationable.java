package de.tobias.playpad.pad.content.play;

import de.tobias.playpad.pad.PadSettings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.util.Duration;

public interface Durationable {

	Duration getDuration();

	Duration getPosition();

	ReadOnlyObjectProperty<Duration> durationProperty();

	ReadOnlyObjectProperty<Duration> positionProperty();

	default Duration getRemaining(PadSettings padSettings) {
		if (!padSettings.isLoop()) {
			final Duration position = getPosition();
			final Duration duration = getDuration();

			if (position != null && duration != null) {
				return duration.subtract(position);
			}
		}
		return null;
	}
}
