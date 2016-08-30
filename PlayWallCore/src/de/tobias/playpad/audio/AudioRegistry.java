package de.tobias.playpad.audio;

import de.tobias.playpad.registry.DefaultComponentRegistry;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;

public class AudioRegistry extends DefaultComponentRegistry<AudioHandlerConnect> {

	public AudioRegistry() {
		super("Audio Handler");
	}

	public AudioHandlerConnect getCurrentAudioHandler() {
		try {
			return getComponent(Profile.currentProfile().getProfileSettings().getAudioClass());
		} catch (NoSuchComponentException e) {
			return getDefault();
		}
	}
}
