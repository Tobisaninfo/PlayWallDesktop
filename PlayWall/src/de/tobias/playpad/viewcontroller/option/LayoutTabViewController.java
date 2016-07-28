package de.tobias.playpad.viewcontroller.option;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.layout.GlobalLayout;
import de.tobias.playpad.layout.LayoutConnect;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.GlobalLayoutViewController;
import de.tobias.playpad.viewcontroller.SettingsTabViewController;
import de.tobias.playpad.viewcontroller.cell.DisplayableCell;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

public class LayoutTabViewController extends SettingsTabViewController {

	@FXML private VBox layoutContainer;
	@FXML private ComboBox<LayoutConnect> layoutTypeComboBox;
	private GlobalLayoutViewController globalLayoutViewController;

	public LayoutTabViewController() {
		super("layoutTab", "de/tobias/playpad/assets/view/option/", PlayPadMain.getUiResourceBundle());

		String layoutType = Profile.currentProfile().getProfileSettings().getLayoutType();
		try {
			layoutTypeComboBox.setValue(PlayPadPlugin.getRegistryCollection().getLayouts().getComponent(layoutType));
		} catch (NoSuchComponentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void init() {
		// Layout
		layoutTypeComboBox.getItems().setAll(PlayPadPlugin.getRegistryCollection().getLayouts().getComponents());
		layoutTypeComboBox.valueProperty().addListener((a, b, c) ->
		{
			String type = c.getType();

			Profile.currentProfile().getProfileSettings().setLayoutType(type);
			GlobalLayout layout = Profile.currentProfile().getLayout(type);
			try {
				setLayoutController(c.getGlobalLayoutViewController(layout));
			} catch (Exception e) {
				e.printStackTrace();
				showErrorMessage(Localization.getString(Strings.Error_Layout_Load, e.getMessage()));
			}
		});

		layoutTypeComboBox.setCellFactory((list) -> new DisplayableCell<>());
		layoutTypeComboBox.setButtonCell(new DisplayableCell<>());
	}

	private void setLayoutController(GlobalLayoutViewController globalLayoutViewController) {
		if (this.globalLayoutViewController != null)
			layoutContainer.getChildren().remove(this.globalLayoutViewController.getParent());

		if (globalLayoutViewController != null) {
			this.globalLayoutViewController = globalLayoutViewController;
			layoutContainer.getChildren().add(globalLayoutViewController.getParent());
		}
	}

	@Override
	public void loadSettings(Profile profile) {}

	@Override
	public void saveSettings(Profile profile) {}

	@Override
	public boolean needReload() {
		return true;
	}

	@Override
	public void reload(Profile profile, Project project, IMainViewController controller) {
		controller.loadUserCss();
	}

	@Override
	public boolean validSettings() {
		return true;
	}

	@Override
	public String name() {
		return Localization.getString(Strings.UI_Window_Settings_Layout_Title);
	}
}
