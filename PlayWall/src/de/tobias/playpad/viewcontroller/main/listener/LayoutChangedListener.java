package de.tobias.playpad.viewcontroller.main.listener;

import de.tobias.playpad.view.main.MainLayoutHandler;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;

import java.util.List;

public class LayoutChangedListener {

	public void handle(List<MainLayoutHandler> runnables, MenuToolbarViewController oldToolbar, MenuToolbarViewController newToolbar) {
		for (MainLayoutHandler run : runnables) {
			run.handle(oldToolbar, newToolbar);
		}
	}
}
