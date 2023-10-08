package de.tobias.playpad.trigger;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.plugin.PadListener;
import de.tobias.playpad.volume.VolumeFilter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VolumeTriggerVolumeFilter implements VolumeFilter, PadListener {

	private static VolumeTriggerVolumeFilter instance;

	public static VolumeTriggerVolumeFilter getInstance() {
		if (instance == null) {
			instance = new VolumeTriggerVolumeFilter();
		}
		return instance;
	}

	private VolumeTriggerVolumeFilter() {
		// nothing to do
	}

	private final Map<UUID, Double> volumes = new HashMap<>();

	@Override
	public double getVolume(Pad pad) {
		return volumes.getOrDefault(pad.getUuid(), 1.0);
	}

	public void setVolume(Pad pad, double newVolume) {
		volumes.put(pad.getUuid(), newVolume);
	}

	// Pad Listener


	@Override
	public void onNameChanged(Pad pad, String oldValue, String newValue) {
		// nothing to do
	}

	@Override
	public void onStatusChange(Pad pad, PadStatus newValue) {
		if (newValue == PadStatus.READY) {
			volumes.remove(pad.getUuid());
		}
	}
}
