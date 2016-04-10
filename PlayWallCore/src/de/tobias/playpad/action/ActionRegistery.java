package de.tobias.playpad.action;

import java.util.HashMap;
import java.util.Set;

public class ActionRegistery {

	private static HashMap<String, ActionConnect> actions;

	static {
		actions = new HashMap<>();
	}

	public static void registerActionConnect(ActionConnect connect) {
		actions.put(connect.getType(), connect);
	}

	public static ActionConnect getActionConnect(String type) {
		if (actions.containsKey(type)) {
			return actions.get(type);
		} else {
			throw new IllegalArgumentException("Type of ActionConnect does not exists: " + type);
		}
	}

	public static Set<String> getTypes() {
		return actions.keySet();
	}
}
