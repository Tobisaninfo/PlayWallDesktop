package de.tobias.playpad.tigger;

import java.util.HashMap;
import java.util.Set;

@Deprecated
public class TriggerRegistry {

	private static HashMap<String, TriggerItemConnect> triggers = new HashMap<>();;

	public static void register(TriggerItemConnect connect) {
		triggers.put(connect.getType(), connect);
	}

	public static TriggerItemConnect getTriggerConnect(String type) {
		if (triggers.containsKey(type)) {
			return triggers.get(type);
		}
		return null;
	}

	public static Set<String> getTypes() {
		return triggers.keySet();
	}
}
