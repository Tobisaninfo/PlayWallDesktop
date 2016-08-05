package de.tobias.playpad.layout.desktop;

import java.util.Stack;

import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.view.main.MainLayoutConnect;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;

public class DesktopMainLayoutConnect implements MainLayoutConnect {

	private DesktopMenuToolbarViewController desktopMenuToolbarViewController;

	private Stack<IPadViewV2> recyclingStack;

	public DesktopMainLayoutConnect() {
		recyclingStack = new Stack<>();
	}

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
		if (!recyclingStack.isEmpty()) {
			return recyclingStack.pop();
		}
		return new DesktopPadView();
	}

	@Override
	public void recyclePadView(IPadViewV2 padView) {
		recyclingStack.push(padView);
	}

	@Override
	public String getStylesheet() {
		return null;
	}

}
