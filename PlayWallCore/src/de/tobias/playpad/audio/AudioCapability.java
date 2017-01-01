package de.tobias.playpad.audio;

import de.tobias.playpad.pad.content.play.Equalizeable;

import java.util.ArrayList;
import java.util.List;

public class AudioCapability {

	public static final AudioCapability EQUALIZER = new AudioCapability("EQUALIZER", Equalizeable.class);
	public static final AudioCapability SOUNDCARD = new AudioCapability("SOUNDCARD", Soundcardable.class);

	private String name;
	private Class<? extends AudioFeature> clazz;

	private static List<AudioCapability> audioCapabilityList;

	static {
		audioCapabilityList = new ArrayList<>();
		audioCapabilityList.add(EQUALIZER);
		audioCapabilityList.add(SOUNDCARD);
	}

	private AudioCapability(String name, Class<? extends AudioFeature> clazz) {
		this.name = name;
		this.clazz = clazz;
	}

	public String getName() {
		return name;
	}

	public Class<? extends AudioFeature> getAudioFeature() {
		return clazz;
	}

	public static List<AudioCapability> getFeatures() {
		return audioCapabilityList;
	}

}
