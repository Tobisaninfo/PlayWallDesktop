package de.tobias.playpad.pad.content.play;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.util.Duration;

public interface Durationable {

	Duration getDuration();

	Duration getPosition();

	ReadOnlyObjectProperty<Duration> durationProperty();

	ReadOnlyObjectProperty<Duration> positionProperty();
}
