package de.tobias.playpad.audio;

import javafx.beans.property.DoubleProperty;

public interface Peakable {

	enum Channel {

		LEFT,
		RIGHT
	}

	DoubleProperty audioLevelProperty(Channel channel);

	double getAudioLevel(Channel channel);
}
