package de.tobias.playpad.layout.touch;

import java.util.Stack;

import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.view.main.MainLayoutConnect;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;
import de.tobias.utils.util.Localization;

/**
 * Touch Implementierung des Main Layout.
 * 
 * @author tobias
 *
 * @since 5.1.0
 */
public class TouchMainLayoutConnect implements MainLayoutConnect {

	private static final String TYPE = "Touch";

	private TouchMenuToolbarViewController touchMainLayoutConnect;

	private Stack<IPadViewV2> recyclingStack;

	public TouchMainLayoutConnect() {
		recyclingStack = new Stack<>();
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String name() {
		return Localization.getString(Strings.MainLayout_Touch);
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
		if (!recyclingStack.isEmpty()) {
			return recyclingStack.pop();
		}
		return new TouchPadView();
	}

	@Override
	public void recyclePadView(IPadViewV2 padView) {
		recyclingStack.push(padView);
	}

	@Override
	public String getStylesheet() {
		return "de/tobias/playpad/assets/style/touch.css";
	}

}
