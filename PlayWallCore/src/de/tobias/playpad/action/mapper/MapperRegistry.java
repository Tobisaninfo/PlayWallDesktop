package de.tobias.playpad.action.mapper;

import java.util.HashMap;
import java.util.Set;

import de.tobias.playpad.viewcontroller.IMapperOverviewViewController;

@Deprecated
public class MapperRegistry {

	private static HashMap<String, MapperConnect> mappers;

	static {
		mappers = new HashMap<>();
	}

	public static void registerMapperConnect(MapperConnect connect) {
		mappers.put(connect.getType(), connect);
	}

	public static MapperConnect getMapperConnect(String type) {
		if (mappers.containsKey(type)) {
			return mappers.get(type);
		} else {
			throw new IllegalArgumentException("Tpye of MapperConnect does not exists: " + type);
		}
	}

	public static Set<String> getTypes() {
		return mappers.keySet();
	}

	//TODO New Implementation
	private static IMapperOverviewViewController controllerInstance;

	public static void setOverviewViewController(IMapperOverviewViewController controller) {
		MapperRegistry.controllerInstance = controller;
	}

	public static IMapperOverviewViewController getOverviewViewControllerInstance() {
		return controllerInstance;
	}
}
