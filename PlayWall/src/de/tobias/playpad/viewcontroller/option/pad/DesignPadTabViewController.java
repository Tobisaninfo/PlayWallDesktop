package de.tobias.playpad.viewcontroller.option.pad;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.action.actions.cart.CartAction;
import de.tobias.playpad.action.factory.CartActionFactory;
import de.tobias.playpad.design.modern.ModernCartDesign2;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.design.ModernCartDesignViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class DesignPadTabViewController extends PadSettingsTabViewController {

	@FXML private VBox layoutContainer;
	@FXML private CheckBox enableLayoutCheckBox;

	private ModernCartDesignViewController layoutViewController;

	private Pad pad;

	DesignPadTabViewController(Pad pad) {
		load("de/tobias/playpad/assets/view/option/pad/", "layoutTab", PlayPadMain.getUiResourceBundle());
		this.pad = pad;
	}

	@Override
	public void init() {
		enableLayoutCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			PadSettings padSettings = pad.getPadSettings();
			if (c && !padSettings.isCustomDesign()) {
				try {
					padSettings.setCustomDesign(true);

					ModernCartDesign2 layout = padSettings.getDesign();
					layout.copyGlobalLayout(Profile.currentProfile().getProfileSettings().getDesign());

					setLayoutViewController(pad);
				} catch (Exception e) {
					showErrorMessage(Localization.getString(Strings.Error_Standard_Gen, e.getLocalizedMessage()));
					e.printStackTrace();
				}
			} else if (!c && padSettings.isCustomDesign()) {
				padSettings.setCustomDesign(false);
				setLayoutViewController(null);
			}
		});
	}

	@Override
	public String getName() {
		return Localization.getString(Strings.UI_Window_PadSettings_Layout_Title);
	}

	@Override
	public void loadSettings(Pad pad) {
		PadSettings padSettings = pad.getPadSettings();

		enableLayoutCheckBox.setSelected(padSettings.isCustomDesign());
		if (padSettings.isCustomDesign()) {
			setLayoutViewController(pad);
		}
	}

	private void setLayoutViewController(Pad pad) {
		try {
			ModernCartDesign2 design = pad.getPadSettings().getDesign();

			ModernCartDesignViewController controller = new ModernCartDesignViewController(design);
			layoutContainer.getChildren().setAll(controller.getParent());
		} catch (NoSuchComponentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			showErrorMessage(Localization.getString(Strings.Error_Layout_Load, e.getMessage()));
		}
	}

	@Override
	public void saveSettings(Pad pad) {
		// CSS
		IMainViewController mainViewController = PlayPadPlugin.getImplementation().getMainViewController();
		mainViewController.loadUserCss();

		try {
			// Mapping Auto Matched Colors
			Mapping activeMapping = Profile.currentProfile().getMappings().getActiveMapping();
			List<CartAction> actions = activeMapping.getActions(PlayPadPlugin.getRegistryCollection().getActions().getFactory(CartActionFactory.class));
			// Update die Mapper der CartAction
			actions.stream().filter(action -> action.getPad() != null).filter(action -> action.getPad().getPosition() == pad.getPosition())
					.forEach(item -> item.init(pad.getProject(), mainViewController));
		} catch (NoSuchComponentException e) {
			e.printStackTrace();
		}
	}
}
