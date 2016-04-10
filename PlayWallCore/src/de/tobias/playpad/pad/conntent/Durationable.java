package de.tobias.playpad.pad.conntent;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.util.Duration;

public interface Durationable {

	public Duration getDuration();

	public Duration getPosition();

	public ReadOnlyObjectProperty<Duration> durationProperty();

	public ReadOnlyObjectProperty<Duration> positionProperty();
}
