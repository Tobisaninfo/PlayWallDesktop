package de.tobias.playpad.audio;

import javafx.scene.media.AudioEqualizer;

public interface Equalizable extends AudioFeature {

	public AudioEqualizer getAudioEqualizer();
}
