package de.tobias.playpad.audio;

public class AudioCapability {

	public static final AudioCapability EQUALIZER = new AudioCapability("EQUALIZER", Equalizable.class);
	public static final AudioCapability SOUNDCARD = new AudioCapability("SOUNDCARD", Soundcardable.class);

	private String name;
	private Class<? extends AudioFeature> clazz;

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

	public static AudioCapability[] getFeatures() {
		return new AudioCapability[] { EQUALIZER, SOUNDCARD };
	}
}