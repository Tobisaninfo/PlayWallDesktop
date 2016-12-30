package de.tobias.playpad.volume;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.settings.Profile;

public class GlobalVolume implements VolumeFilter {

	@Override
	public double getVolume(Pad pad) {
		if (Profile.currentProfile() != null) {
			return Profile.currentProfile().getProfileSettings().getVolume();
		} else {
			return 1.0;
		}
	}
}
