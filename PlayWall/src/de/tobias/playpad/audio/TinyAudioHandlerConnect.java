package de.tobias.playpad.audio;

import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.viewcontroller.AudioTypeViewController;
import de.tobias.playpad.viewcontroller.audio.TinySoundSettingsViewController;

public class TinyAudioHandlerConnect extends AudioHandlerConnect implements AutoCloseable {

	@Override
	public AudioHandler createAudioHandler(PadContent content) {
		return new TinyAudioHandler(content);
	}

	@Override
	public AudioTypeViewController getAudioViewController() {
		return new TinySoundSettingsViewController();
	}

	@Override
	public void close() throws Exception {
		TinyAudioHandler.shutdown();
	}
}
