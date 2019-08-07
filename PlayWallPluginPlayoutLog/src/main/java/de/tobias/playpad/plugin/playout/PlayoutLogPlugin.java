package de.tobias.playpad.plugin.playout;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.plugins.PluginArtifact;
import de.thecodelabs.plugins.PluginDescriptor;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.PlayPadPluginStub;
import de.tobias.playpad.plugin.WindowListener;
import de.tobias.playpad.plugin.playout.viewcontroller.PlayoutLogViewController;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.scene.control.MenuItem;

@SuppressWarnings("unused")
public class PlayoutLogPlugin implements PlayPadPluginStub, PluginArtifact {

	private Module module;

	@Override
	public void startup(PluginDescriptor descriptor) {
		Localization.addResourceBundle("lang/playoutlog", getClass().getClassLoader());
		module = new Module(descriptor.getName(), descriptor.getArtifactId());
		Logger.debug("Enable Playout Log Plugin");

		PlayPadPlugin.getInstance().addMainViewListener(new WindowListener<IMainViewController>() {

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
				});
			}
		});
	}

	@Override
	public void shutdown() {
		Logger.debug("Disable Playout Log Plugin");
	}

	@Override
	public Module getModule() {
		return module;
	}
}
