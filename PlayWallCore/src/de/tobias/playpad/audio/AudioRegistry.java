package de.tobias.playpad.audio;

import de.tobias.playpad.registry.DefaultComponentRegistry;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.profile.Profile;

@Deprecated
public class AudioRegistry extends DefaultComponentRegistry<AudioHandlerFactory> {

	public AudioRegistry() {
		super("Audio Handler");
	}

	public AudioHandlerFactory getCurrentAudioHandler() {
		try {
			return getFactory(Profile.currentProfile().getProfileSettings().getAudioClass());
		} catch (NoSuchComponentException e) {
			return getDefault();
		}
	}
}
