package de.tobias.playpad.viewcontroller.option.pad;

import java.util.List;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.action.cartaction.CartAction;
import de.tobias.playpad.action.connect.CartActionConnect;
import de.tobias.playpad.layout.CartLayout;
import de.tobias.playpad.layout.LayoutRegistry;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.CartLayoutViewController;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

public class LayoutPadTabViewController extends PadSettingsTabViewController {

	@FXML private VBox layoutContainer;
	@FXML private CheckBox enableLayoutCheckBox;
	private CartLayoutViewController layoutViewController;

	private Pad pad;

	public LayoutPadTabViewController(Pad pad) {
		super("layoutTab", "de/tobias/playpad/assets/view/option/pad/", PlayPadMain.getUiResourceBundle());
		this.pad = pad;
	}

	private void setLayoutController(CartLayoutViewController cartLayoutViewController) {
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
			if (c && !pad.isCustomLayout()) {
				try {
					pad.setCustomLayout(true);

					String layoutType = Profile.currentProfile().getProfileSettings().getLayoutType();
					CartLayout layout = pad.getLayout(layoutType);
					layout.copyGlobalLayout(Profile.currentProfile().getLayout(layoutType));

					setLayoutViewController(pad);
				} catch (Exception e) {
					showErrorMessage(Localization.getString(Strings.Error_Standard_Gen, e.getLocalizedMessage()));
					e.printStackTrace();
				}
			} else if (!c && pad.isCustomLayout()) {
				pad.setCustomLayout(false);
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
		enableLayoutCheckBox.setSelected(pad.isCustomLayout());
		if (pad.isCustomLayout()) {
			setLayoutViewController(pad);
		}
	}

	private void setLayoutViewController(Pad pad) {
		try {
			String layoutType = Profile.currentProfile().getProfileSettings().getLayoutType();
			CartLayout layout = pad.getLayout(layoutType);

			CartLayoutViewController controller = LayoutRegistry.getLayout(layoutType).getCartLayoutViewController(layout);
			setLayoutController(controller);
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
