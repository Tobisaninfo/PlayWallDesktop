package de.tobias.playpad.layout.desktop;

import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.view.main.MainLayoutConnect;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;

public class DesktopMainLayoutConnect implements MainLayoutConnect {

	private DesktopMenuToolbarViewController desktopMenuToolbarViewController;

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
		if (desktopMenuToolbarViewController == null) {
			desktopMenuToolbarViewController = new DesktopMenuToolbarViewController(mainViewRef);
		}
		return desktopMenuToolbarViewController;
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
