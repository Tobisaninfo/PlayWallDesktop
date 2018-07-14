package de.tobias.playpad.pad.content.play;

import de.tobias.playpad.audio.AudioFeature;
import javafx.scene.media.AudioEqualizer;

public interface Equalizeable extends AudioFeature {

	AudioEqualizer getAudioEqualizer();
}
