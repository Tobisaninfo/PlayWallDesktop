package de.tobias.playpad.viewcontroller.main;

import java.util.List;

import de.tobias.playpad.view.main.MainLayoutHandler;

public class LayoutChangedListener {

	public void handle(List<MainLayoutHandler> runnables, MenuToolbarViewController oldToolbar, MenuToolbarViewController newToolbar) {
		for (MainLayoutHandler run : runnables) {
			run.handle(oldToolbar, newToolbar);
		}
	}
}
