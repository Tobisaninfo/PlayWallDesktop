package de.tobias.playpad.layout.touch;

import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.view.main.MainLayoutConnect;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;

public class TouchMainLayoutConnect implements MainLayoutConnect {

	private TouchMenuToolbarViewController touchMainLayoutConnect;

	@Override
	public String getType() {
		return "Touch";
	}

	@Override
	public String name() {
		return "Touch Modus"; // TODO Localize
	}

	@Override
	public MenuToolbarViewController createMenuToolbar(IMainViewController mainViewRef) {
		if (touchMainLayoutConnect == null) {
			touchMainLayoutConnect = new TouchMenuToolbarViewController(mainViewRef);
		}

		return touchMainLayoutConnect;
	}

	@Override
	public IPadViewV2 createPadView() {
		return new TouchPadView();
	}

	@Override
	public String getStylesheet() {
		return "de/tobias/playpad/assets/style/touch.css";
	}

}
