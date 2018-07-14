package de.tobias.playpad.audio;

import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;

public class JavaFXHandlerFactory extends AudioHandlerFactory {

	public JavaFXHandlerFactory(String type) {
		super(type);
	}

	@Override
	public AudioHandler createAudioHandler(PadContent content) {
		return new JavaFXAudioHandler(content);
	}

	@Override
	public boolean isFeatureAvailable(AudioCapability audioCapability) {
		for (Class<?> clazz : JavaFXAudioHandler.class.getInterfaces()) {
			if (clazz.equals(audioCapability.getAudioFeature()))
				return true;
		}
		return false;
	}

	@Override
	public AudioHandlerViewController getAudioFeatureSettings(AudioCapability audioCapablility) {
		return null;
	}
}
