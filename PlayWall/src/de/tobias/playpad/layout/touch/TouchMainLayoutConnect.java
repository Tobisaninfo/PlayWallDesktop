package de.tobias.playpad.layout.touch;

import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.view.main.MainLayoutConnect;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;

public class TouchMainLayoutConnect implements MainLayoutConnect {

	@Override
	public String getType() {
		return "touch";
	}

	@Override
	public String name() {
		return null;
	}

	@Override
	public MenuToolbarViewController createMenuToolbar(IMainViewController mainViewRef) {
		return new TouchMenuToolbarViewController(mainViewRef);
	}

	@Override
	public IPadViewV2 createPadView() {
		return new TouchPadView();
	}

	
}
