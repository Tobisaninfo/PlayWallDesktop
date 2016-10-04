package de.tobias.playpad.audio;

import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;

public class JavaFXHandlerConnect extends AudioHandlerConnect {

	@Override
	public AudioHandler createAudioHandler(PadContent content) {
		return new JavaFXAudioHandler(content);
	}

	@Override
	public AudioHandlerViewController getAudioHandlerSettingsViewController() {
		return null;
	}
	
	@Override
	public String getType() {
		return JavaFXAudioHandler.TYPE;
	}
	
	@Override
	public boolean isFeatureAvaiable(AudioCapability audioCapability) {
		for (Class<?> clazz : JavaFXAudioHandler.class.getInterfaces()) {
			if (clazz.equals(audioCapability.getAudioFeature()))
				return true;
		}
		return false;
	}
}
