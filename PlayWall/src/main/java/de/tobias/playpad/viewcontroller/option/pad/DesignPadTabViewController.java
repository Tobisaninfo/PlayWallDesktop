package de.tobias.playpad.viewcontroller.option.pad;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.design.modern.model.ModernCartDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.design.ModernCartDesignViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class DesignPadTabViewController extends PadSettingsTabViewController {

	@FXML
	private VBox layoutContainer;

	private final Pad pad;

	DesignPadTabViewController(Pad pad) {
		load("view/option/pad", "LayoutTab", Localization.getBundle());
		this.pad = pad;
	}

	@Override
	public void init() {
	}

	@Override
	public String getName() {
		return Localization.getString(Strings.UI_WINDOW_PAD_SETTINGS_LAYOUT_TITLE);
	}

	@Override
	public void loadSettings(Pad pad) {
		setLayoutViewController(pad);
	}

	private void setLayoutViewController(Pad pad) {
		if (pad != null) {
			try {
				ModernCartDesign design = pad.getPadSettings().getDesign();

				ModernCartDesignViewController controller = new ModernCartDesignViewController(design);
				layoutContainer.getChildren().setAll(controller.getParent());
			} catch (Exception e) {
				Logger.error(e);
				showErrorMessage(Localization.getString(Strings.ERROR_LAYOUT_LOAD, e.getMessage()));
			}
		} else {
			layoutContainer.getChildren().clear();
		}
	}

	@Override
	public void saveSettings(Pad pad) {
		// CSS
		IMainViewController mainViewController = PlayPadPlugin.getInstance().getMainViewController();
		mainViewController.loadUserCss();
	}
}
