package de.tobias.playpad.audio.windows;

import de.tobias.playpad.audio.AudioCapability;
import de.tobias.playpad.audio.AudioHandler;
import de.tobias.playpad.audio.AudioHandlerFactory;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;

public class NativeAudioWinHandlerFactory extends AudioHandlerFactory {

	public NativeAudioWinHandlerFactory(String type) {
		super(type);
	}

	@Override
	public AudioHandler createAudioHandler(PadContent content) {
		return new NativeAudioWinHandler(content);
	}

	@Override
	public boolean isFeatureAvailable(AudioCapability audioCapability) {
		for (Class<?> clazz : NativeAudioWinHandler.class.getInterfaces()) {
			if (clazz.equals(audioCapability.getAudioFeature()))
				return true;
		}
		return false;
	}

	@Override
	public AudioHandlerViewController getAudioFeatureSettings(AudioCapability audioCapablility) {
		if (audioCapablility == AudioCapability.SOUNDCARD) {
			return new NativeAudioSettingsViewController();
		}
		return null;
	}
}
