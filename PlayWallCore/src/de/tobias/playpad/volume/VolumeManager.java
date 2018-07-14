package de.tobias.playpad.volume;

import de.tobias.playpad.pad.Pad;

import java.util.ArrayList;
import java.util.List;

public class VolumeManager {

	private static final VolumeManager instance;

	static {
		instance = new VolumeManager();
	}

	public static VolumeManager getInstance() {
		return instance;
	}

	private List<VolumeFilter> filters;

	private VolumeManager() {
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
