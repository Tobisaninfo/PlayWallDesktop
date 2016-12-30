package de.tobias.playpad.volume;

import de.tobias.playpad.pad.Pad;

public class PadVolume implements VolumeFilter {

	@Override
	public double getVolume(Pad pad) {
		return pad.getPadSettings().getVolume();
	}
}
