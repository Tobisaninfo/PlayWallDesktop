package de.tobias.playpad.audio;

import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.viewcontroller.AudioTypeViewController;

public class JavaFXHandlerConnect extends AudioHandlerConnect {

	@Override
	public AudioHandler createAudioHandler(PadContent content) {
		return new JavaFXAudioHandler(content);
	}

	@Override
	public AudioTypeViewController getAudioViewController() {
		return null;
	}
}
