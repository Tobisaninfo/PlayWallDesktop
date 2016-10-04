package de.tobias.playpad.audio;

import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;
import de.tobias.playpad.viewcontroller.audio.ClipSettingsViewController;

public class ClipAudioHandlerConnect extends AudioHandlerConnect implements AutoCloseable {

	@Override
	public AudioHandler createAudioHandler(PadContent content) {
		return new ClipAudioHandler(content);
	}

	@Override
	public AudioHandlerViewController getAudioHandlerSettingsViewController() {
		return new ClipSettingsViewController();
	}

	@Override
	public String getType() {
		return ClipAudioHandler.TYPE;
	}

	@Override
	public void close() throws Exception {
		ClipAudioHandler.shutdown();
	}

	@Override
	public boolean isFeatureAvaiable(AudioCapability audioCapability) {
		for (Class<?> clazz : ClipAudioHandler.class.getInterfaces()) {
			if (clazz.equals(audioCapability.getAudioFeature()))
				return true;
		}
		return false;
	}
}
