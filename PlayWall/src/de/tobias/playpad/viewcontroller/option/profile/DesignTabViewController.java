package de.tobias.playpad.viewcontroller.option.profile;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.design.DesignFactory;
import de.tobias.playpad.design.GlobalDesign;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.viewcontroller.GlobalDesignViewController;
import de.tobias.playpad.viewcontroller.cell.DisplayableCell;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.IProfileReloadTask;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import de.tobias.utils.util.Localization;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

public class DesignTabViewController extends ProfileSettingsTabViewController implements IProfileReloadTask {

	@FXML private VBox layoutContainer;
	@FXML private ComboBox<DesignFactory> layoutTypeComboBox;
	private GlobalDesignViewController globalLayoutViewController;

	public DesignTabViewController() {
		super("layoutTab", "de/tobias/playpad/assets/view/option/profile/", PlayPadMain.getUiResourceBundle());

		String layoutType = Profile.currentProfile().getProfileSettings().getLayoutType();
		try {
			layoutTypeComboBox.setValue(PlayPadPlugin.getRegistryCollection().getDesigns().getFactory(layoutType));
		} catch (NoSuchComponentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void init() {
		// Layout
		layoutTypeComboBox.getItems().setAll(PlayPadPlugin.getRegistryCollection().getDesigns().getComponents());
		layoutTypeComboBox.valueProperty().addListener((a, b, c) ->
		{
			String type = c.getType();

			Profile.currentProfile().getProfileSettings().setLayoutType(type);
			GlobalDesign layout = Profile.currentProfile().getLayout(type);
			try {
				setLayoutController(c.getGlobalDesignViewController(layout));
			} catch (Exception e) {
				e.printStackTrace();
				showErrorMessage(Localization.getString(Strings.Error_Layout_Load, e.getMessage()));
			}
		});

		layoutTypeComboBox.setCellFactory((list) -> new DisplayableCell<>());
		layoutTypeComboBox.setButtonCell(new DisplayableCell<>());
	}

	private void setLayoutController(GlobalDesignViewController globalLayoutViewController) {
		if (this.globalLayoutViewController != null)
			layoutContainer.getChildren().remove(this.globalLayoutViewController.getParent());

		if (globalLayoutViewController != null) {
			this.globalLayoutViewController = globalLayoutViewController;
			layoutContainer.getChildren().add(globalLayoutViewController.getParent());
		}
	}

	@Override
	public void loadSettings(Profile profile) {
	}

	@Override
	public void saveSettings(Profile profile) {
	}

	@Override
	public boolean needReload() {
		return true;
	}

	@Override
	public Task<Void> getTask(ProfileSettings settings, Project project, IMainViewController controller) {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				updateTitle(name());
				updateProgress(-1, -1);

				Platform.runLater(() -> controller.loadUserCss());
				return null;
			}
		};
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
