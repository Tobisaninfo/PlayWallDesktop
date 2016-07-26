package de.tobias.playpad.audio;

import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.viewcontroller.AudioTypeViewController;
import de.tobias.playpad.viewcontroller.audio.ClipSettingsViewController;

public class ClipAudioHandlerConnect extends AudioHandlerConnect implements AutoCloseable {

	@Override
	public AudioHandler createAudioHandler(PadContent content) {
		return new ClipAudioHandler(content);
	}

	@Override
	public AudioTypeViewController getAudioViewController() {
		return new ClipSettingsViewController();
	}
	
	@Override
	public void close() throws Exception {
		TinyAudioHandler.shutdown();
	}

}
