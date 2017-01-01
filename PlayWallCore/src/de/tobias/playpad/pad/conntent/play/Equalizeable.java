package de.tobias.playpad.pad.conntent.play;

import de.tobias.playpad.audio.AudioFeature;
import javafx.scene.media.AudioEqualizer;

public interface Equalizeable extends AudioFeature {

	AudioEqualizer getAudioEqualizer();
}
