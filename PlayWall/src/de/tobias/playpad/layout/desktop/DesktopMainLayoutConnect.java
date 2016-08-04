package de.tobias.playpad.layout.desktop;

import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.view.main.MainLayoutConnect;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;

public class DesktopMainLayoutConnect implements MainLayoutConnect {

	@Override
	public String getType() {
		return "Desktop";
	}

	@Override
	public String name() {
		return "Desktop Modus"; // TODO Localize
	}

	@Override
	public MenuToolbarViewController createMenuToolbar(IMainViewController mainViewRef) {
		return new DesktopMenuToolbarViewController(mainViewRef);
	}

	@Override
	public IPadViewV2 createPadView() {
		return new DesktopPadView();
	}
	
	@Override
	public String getStylesheet() {
		return null;
	}

}
