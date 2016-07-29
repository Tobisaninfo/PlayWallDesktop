package de.tobias.playpad.design;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

@Deprecated
public class LayoutRegistry {

	static {
		layouts = new HashMap<>();
	}

	private static String defaultLayout;
	private static HashMap<String, DesignConnect> layouts;

	public static void registerLayout(DesignConnect layoutConnect) {
		layouts.put(layoutConnect.getType(), layoutConnect);
	}

	public static Set<String> getTypes() {
		return layouts.keySet();
	}

	public static DesignConnect getLayout(String type) {
		return layouts.get(type);
	}

	public static Collection<DesignConnect> getValues() {
		return layouts.values();
	}

	public static String getDefaultLayout() {
		return defaultLayout;
	}

	public static void setDefaultLayout(String defaultLayout) {
		LayoutRegistry.defaultLayout = defaultLayout;
	}
}
