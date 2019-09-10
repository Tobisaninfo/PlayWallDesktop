package de.tobias.playpad.plugin.playout.viewcontroller;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playpad.plugin.MainWindowListener;
import de.tobias.playpad.plugin.playout.log.LogSeason;
import de.tobias.playpad.plugin.playout.log.LogSessionListener;
import de.tobias.playpad.viewcontroller.main.IMainViewController;

public class PlayoutLogStatusIconListener implements LogSessionListener, MainWindowListener {
	private FontIcon logIcon;

	@Override
	public void playoutLogStarted(LogSeason logSeason) {
		logIcon.setVisible(true);
	}

	@Override
	public void playoutLogStopped(LogSeason logSeason) {
		logIcon.setVisible(false);
	}

	@Override
	public void onInit(IMainViewController mainViewControllerListener) {
		// LogIcon
		logIcon = new FontIcon(FontAwesomeType.LIST);
		mainViewControllerListener.performLayoutDependedAction((oldToolbar, newToolbar) -> {
			if (oldToolbar != null) {
				oldToolbar.removeToolbarItem(logIcon);
			}
			newToolbar.addToolbarItem(logIcon);
		});
	}
}
