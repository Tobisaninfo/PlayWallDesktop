package de.tobias.playpad.audio;

import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;

@Deprecated
public class ClipAudioHandlerFactory extends AudioHandlerFactory implements AutoCloseable {

	public ClipAudioHandlerFactory(String type) {
		super(type);
	}

	@Override
	public AudioHandler createAudioHandler(PadContent content) {
		return new ClipAudioHandler(content);
	}

	@Override
	public void close() throws Exception {
		ClipAudioHandler.shutdown();
	}

	@Override
	public boolean isFeatureAvailable(AudioCapability audioCapability) {
		for (Class<?> clazz : ClipAudioHandler.class.getInterfaces()) {
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
