package de.tobias.playpad.audio;

import de.tobias.playpad.audio.viewcontroller.TinySoundSettingsViewController;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;

public class TinyAudioHandlerFactory extends AudioHandlerFactory implements AutoCloseable {

	public TinyAudioHandlerFactory(String type) {
		super(type);
	}

	@Override
	public AudioHandler createAudioHandler(PadContent content) {
		return new TinyAudioHandler(content);
	}

	@Override
	public String getType() {
		return TinyAudioHandler.TYPE;
	}

	@Override
	public void close() {
		TinyAudioHandler.shutdown();
	}

	@Override
	public boolean isFeatureAvailable(AudioCapability audioCapability) {
		for (Class<?> clazz : TinyAudioHandler.class.getInterfaces()) {
			if (clazz.equals(audioCapability.getAudioFeature()))
				return true;
		}
		return false;
	}

	@Override
	public AudioHandlerViewController getAudioFeatureSettings(AudioCapability audioCapability) {
		if (audioCapability == AudioCapability.SOUNDCARD) {
			return new TinySoundSettingsViewController();
		}
		return null;
	}
}
