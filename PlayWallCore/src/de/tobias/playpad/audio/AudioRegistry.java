package de.tobias.playpad.audio;

import java.util.HashMap;

import de.tobias.playpad.settings.Profile;

public class AudioRegistry {

	private static String defaultAudioInterface;
	private static HashMap<String, AudioHandlerConnect> audioSystems = new HashMap<>();

	public static void register(AudioHandlerConnect type, String name) {
		audioSystems.put(name, type);
	}

	public static HashMap<String, AudioHandlerConnect> getAudioSystems() {
		return audioSystems;
	}

	public static AudioHandlerConnect geAudioType() {
		String impl = Profile.currentProfile().getProfileSettings().getAudioClass();
		if (audioSystems.containsKey(impl)) {
			return audioSystems.get(impl);			
		} else {
			return audioSystems.get(defaultAudioInterface);
		}
	}

	public static String getDefaultAudioInterface() {
		return defaultAudioInterface;
	}

	public static void setDefaultAudioInterface(String defaultAudioInterface) {
		AudioRegistry.defaultAudioInterface = defaultAudioInterface;
	}
}
