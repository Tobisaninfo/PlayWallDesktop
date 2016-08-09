package de.tobias.playpad.viewcontroller.option.pad;

import java.util.List;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.action.cartaction.CartAction;
import de.tobias.playpad.action.connect.CartActionConnect;
import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.DesignConnect;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.CartDesignViewController;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

public class DesignPadTabViewController extends PadSettingsTabViewController {

	@FXML private VBox layoutContainer;
	@FXML private CheckBox enableLayoutCheckBox;
	private CartDesignViewController layoutViewController;

	private Pad pad;

	public DesignPadTabViewController(Pad pad) {
		super("layoutTab", "de/tobias/playpad/assets/view/option/pad/", PlayPadMain.getUiResourceBundle());
		this.pad = pad;
	}

	private void setLayoutController(CartDesignViewController cartLayoutViewController) {
		if (layoutViewController != null)
			layoutContainer.getChildren().remove(layoutViewController.getParent());

		if (cartLayoutViewController != null) {
			layoutViewController = cartLayoutViewController;
			layoutContainer.getChildren().add(layoutViewController.getParent());
		}
	}

	@Override
	public void init() {
		enableLayoutCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			PadSettings padSettings = pad.getPadSettings();
			if (c && !padSettings.isCustomLayout()) {
				try {
					padSettings.setCustomLayout(true);

					String layoutType = Profile.currentProfile().getProfileSettings().getLayoutType();
					CartDesign layout = padSettings.getLayout(layoutType);
					layout.copyGlobalLayout(Profile.currentProfile().getLayout(layoutType));

					setLayoutViewController(pad);
				} catch (Exception e) {
					showErrorMessage(Localization.getString(Strings.Error_Standard_Gen, e.getLocalizedMessage()));
					e.printStackTrace();
				}
			} else if (!c && padSettings.isCustomLayout()) {
				padSettings.setCustomLayout(false);
				setLayoutController(null);
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

		enableLayoutCheckBox.setSelected(padSettings.isCustomLayout());
		if (padSettings.isCustomLayout()) {
			setLayoutViewController(pad);
		}
	}

	private void setLayoutViewController(Pad pad) {
		try {
			String layoutType = Profile.currentProfile().getProfileSettings().getLayoutType();
			CartDesign layout = pad.getPadSettings().getLayout(layoutType);

			DesignConnect component = PlayPadPlugin.getRegistryCollection().getDesigns().getComponent(layoutType);
			CartDesignViewController controller = component.getCartDesignViewController(layout);
			setLayoutController(controller);
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

		// Mapping Auto Matched Colors
		Mapping activeMapping = Profile.currentProfile().getMappings().getActiveMapping();
		List<CartAction> actions = activeMapping.getActions(CartActionConnect.TYPE);
		// Update die Mapper der CartAction
		actions.stream().filter(item -> item.getCart() == pad.getIndex())
				.forEach(item -> item.initFeedback(pad.getProject(), mainViewController));
	}
}
