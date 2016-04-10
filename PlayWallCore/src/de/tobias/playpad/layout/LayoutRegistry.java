package de.tobias.playpad.layout;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class LayoutRegistry {

	static {
		layouts = new HashMap<>();
	}

	private static String defaultLayout;
	private static HashMap<String, LayoutConnect> layouts;

	public static void registerLayout(LayoutConnect layoutConnect) {
		layouts.put(layoutConnect.getType(), layoutConnect);
	}

	public static Set<String> getTypes() {
		return layouts.keySet();
	}

	public static LayoutConnect getLayout(String type) {
		return layouts.get(type);
	}

	public static Collection<LayoutConnect> getValues() {
		return layouts.values();
	}

	public static String getDefaultLayout() {
		return defaultLayout;
	}

	public static void setDefaultLayout(String defaultLayout) {
		LayoutRegistry.defaultLayout = defaultLayout;
	}
}
