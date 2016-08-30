package de.tobias.playpad.action.mapper;

import de.tobias.playpad.viewcontroller.IMapperOverviewViewController;

@Deprecated
public class MapperRegistry {

	// TODO New Implementation
	private static IMapperOverviewViewController controllerInstance;

	public static void setOverviewViewController(IMapperOverviewViewController controller) {
		MapperRegistry.controllerInstance = controller;
	}

	public static IMapperOverviewViewController getOverviewViewControllerInstance() {
		return controllerInstance;
	}
}
