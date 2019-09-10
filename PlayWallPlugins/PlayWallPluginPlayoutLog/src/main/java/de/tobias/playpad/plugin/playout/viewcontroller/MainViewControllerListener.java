package de.tobias.playpad.plugin.playout.viewcontroller;

import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.plugin.MainWindowListener;
import de.tobias.playpad.plugin.playout.PlayoutLogPlugin;
import de.tobias.playpad.plugin.playout.Strings;
import de.tobias.playpad.settings.keys.KeyCollection;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;
import javafx.scene.control.MenuItem;

public class MainViewControllerListener implements MainWindowListener {
	private MenuItem menuItem;

	@Override
	public void onInit(IMainViewController iMainViewController) {
		iMainViewController.performLayoutDependedAction((oldToolbar, newToolbar) -> {
			if (menuItem == null) {
				menuItem = new MenuItem(Localization.getString(Strings.MENU_ITEM_LOG));
				menuItem.setOnAction(event -> {
					PlayoutLogViewController playoutLogViewController = new PlayoutLogViewController(newToolbar.getContainingWindow());
					playoutLogViewController.getStageContainer().ifPresent(NVCStage::showAndWait);
				});
			}

			if (oldToolbar != null) {
				oldToolbar.removeMenuItem(menuItem);
			}

			newToolbar.addMenuItem(menuItem, MenuType.EXTENSION);
			loadMenuKeyBinding();
		});
	}

	@Override
	public void loadMenuKeyBinding() {
		final KeyCollection keyCollection = PlayPadPlugin.getInstance().getGlobalSettings().getKeyCollection();
		final MenuToolbarViewController menuToolbarController = PlayPadPlugin.getInstance().getMainViewController().getMenuToolbarController();
		menuToolbarController.setKeyBindingForMenu(menuItem, keyCollection.getKey(PlayoutLogPlugin.KEY_COLLECTION_PLAYOUT));

		menuItem.setDisable(false);
	}
}
