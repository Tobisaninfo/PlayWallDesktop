package de.tobias.playpad.audio;

import javafx.beans.property.DoubleProperty;

public interface Peakable {

	public enum Channel {

		LEFT,
		RIGHT;
	}
	
	public DoubleProperty audioLevelProperty(Channel channel);
	
	public double getAudioLevel(Channel channel);
}
