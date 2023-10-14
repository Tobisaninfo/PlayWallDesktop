package de.tobias.playpad.plugin;

import de.tobias.playpad.viewcontroller.main.IMainViewController;

public interface MainWindowListener extends WindowListener<IMainViewController> {

	default void onCurrentPageChanged(int newPage) {
	}

	default void loadMenuKeyBinding() {
	}
}
