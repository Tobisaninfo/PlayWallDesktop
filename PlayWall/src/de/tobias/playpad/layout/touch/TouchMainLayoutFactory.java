package de.tobias.playpad.layout.touch;

import java.util.Stack;

import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.view.IPadView;
import de.tobias.playpad.view.main.MainLayoutFactory;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;
import de.tobias.utils.ui.icon.FontIconType;
import de.tobias.utils.util.Localization;

/**
 * Touch Implementierung des Main Layout.
 * 
 * @author tobias
 *
 * @since 5.1.0
 */
public class TouchMainLayoutFactory extends MainLayoutFactory {

	private TouchMenuToolbarViewController touchMainLayoutConnect;

	private Stack<IPadView> recyclingStack;

	public TouchMainLayoutFactory(String type) {
		super(type);
		recyclingStack = new Stack<>();
	}

	@Override
	public MenuToolbarViewController createMenuToolbar(IMainViewController mainViewRef) {
		if (touchMainLayoutConnect == null) {
			touchMainLayoutConnect = new TouchMenuToolbarViewController(mainViewRef);
		}

		return touchMainLayoutConnect;
	}

	@Override
	public IPadView createPadView() {
		if (!recyclingStack.isEmpty()) {
			return recyclingStack.pop();
		}
		return new TouchPadView();
	}

	@Override
	public void recyclePadView(IPadView padView) {
		recyclingStack.push(padView);
	}

	@Override
	public String getStylesheet() {
		return "de/tobias/playpad/assets/style/touch.css";
	}

}
