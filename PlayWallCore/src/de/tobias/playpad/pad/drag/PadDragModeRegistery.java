package de.tobias.playpad.pad.drag;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

@Deprecated
public class PadDragModeRegistery {

	private static HashMap<String, PadDragMode> padDragMode;

	static {
		padDragMode = new HashMap<>();
	}

	public static void registerActionConnect(PadDragMode mode) {
		padDragMode.put(mode.getType(), mode);
	}

	public static PadDragMode getPadDragMode(String type) {
		if (padDragMode.containsKey(type)) {
			return padDragMode.get(type);
		} else {
			throw new IllegalArgumentException("Tpye of PadDragMode does not exists: " + type);
		}
	}

	public static Set<String> getTypes() {
		return padDragMode.keySet();
	}

	public static Collection<PadDragMode> getValues() {
		return padDragMode.values();
	}
}
