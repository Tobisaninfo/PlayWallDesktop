package de.tobias.playpad.nawin.audio;

import de.tobias.playpad.audio.AudioCapability;
import de.tobias.playpad.audio.AudioHandler;
import de.tobias.playpad.audio.AudioHandlerConnect;
import de.tobias.playpad.nawin.NativeAudioWinHandler;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;

public class NativeAudioWinHandlerConnect extends AudioHandlerConnect {

	@Override
	public AudioHandler createAudioHandler(PadContent content) {
		return new NativeAudioWinHandler(content);
	}

	@Override
	public AudioHandlerViewController getAudioHandlerSettingsViewController() {
		return null;
	}

	@Override
	public String getType() {
		return "NativeWin";
	}
	
	@Override
	public boolean isFeatureAvaiable(AudioCapability audioCapability) {
		for (Class<?> clazz : NativeAudioWinHandler.class.getInterfaces()) {
			if (clazz.equals(audioCapability.getAudioFeature()))
				return true;
		}
		return false;
	}

}
