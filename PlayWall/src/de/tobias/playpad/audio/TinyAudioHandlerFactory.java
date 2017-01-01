package de.tobias.playpad.audio;

import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;
import de.tobias.playpad.viewcontroller.audio.TinySoundSettingsViewController;
import de.tobias.utils.ui.icon.FontIconType;

public class TinyAudioHandlerFactory extends AudioHandlerFactory implements AutoCloseable {

	public TinyAudioHandlerFactory(String type) {
		super(type);
	}

	@Override
	public AudioHandler createAudioHandler(PadContent content) {
		return new TinyAudioHandler(content);
	}

	@Override
	public AudioHandlerViewController getAudioHandlerSettingsViewController() {
		return new TinySoundSettingsViewController();
	}

	@Override
	public String getType() {
		return TinyAudioHandler.TYPE;
	}

	@Override
	public void close() throws Exception {
		TinyAudioHandler.shutdown();
	}

	@Override
	public boolean isFeatureAvaiable(AudioCapability audioCapability) {
		for (Class<?> clazz : TinyAudioHandler.class.getInterfaces()) {
			if (clazz.equals(audioCapability.getAudioFeature()))
				return true;
		}
		return false;
	}
	
	@Override
	public AudioHandlerViewController getAudioFeatureSettings(AudioCapability audioCapablility) {
		if (audioCapablility == AudioCapability.SOUNDCARD) {
			return new TinySoundSettingsViewController();
		}
		return null;
	}
}