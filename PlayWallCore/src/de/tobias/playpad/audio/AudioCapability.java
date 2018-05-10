package de.tobias.playpad.audio;

import de.tobias.playpad.pad.content.play.Equalizeable;
import de.tobias.utils.util.Localization;

import java.util.ArrayList;
import java.util.List;

public class AudioCapability {

	public static final AudioCapability EQUALIZER = new AudioCapability("EQUALIZER", Equalizeable.class);
	public static final AudioCapability SOUNDCARD = new AudioCapability("SOUNDCARD", Soundcardable.class);

	private String nameKey;
	private Class<? extends AudioFeature> clazz;

	private static List<AudioCapability> audioCapabilityList;

	static {
		audioCapabilityList = new ArrayList<>();
		audioCapabilityList.add(EQUALIZER);
		audioCapabilityList.add(SOUNDCARD);
	}

	private AudioCapability(String nameKey, Class<? extends AudioFeature> clazz) {
		this.nameKey = nameKey;
		this.clazz = clazz;
	}

	public String getNameKey() {
		return nameKey;
	}

	public String getName() {
		return Localization.getString(getNameKey());
	}

	public Class<? extends AudioFeature> getAudioFeature() {
		return clazz;
	}

	public static List<AudioCapability> getFeatures() {
		return audioCapabilityList;
	}

}
