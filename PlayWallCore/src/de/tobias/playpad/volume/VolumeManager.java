package de.tobias.playpad.volume;

import java.util.ArrayList;
import java.util.List;

import de.tobias.playpad.pad.Pad;

public class VolumeManager {

	private List<VolumeFilter> filters;

	public VolumeManager() {
		this.filters = new ArrayList<>();
	}

	public void addFilter(VolumeFilter filter) {
		filters.add(filter);
	}

	public void removeFilter(VolumeFilter filter) {
		filters.remove(filter);
	}

	public double computeVolume(Pad pad) {
		double volume = 1;
		for (VolumeFilter filter : filters) {
			volume *= filter.getVolume(pad);

			if (volume == 0) {
				break;
			}
		}
		return volume;
	}

}