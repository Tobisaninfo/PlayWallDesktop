package de.tobias.playpad.viewcontroller.option.profile;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.TimeMode;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.viewcontroller.cell.EnumCell;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import de.tobias.playpad.viewcontroller.settings.FadeViewController;
import de.tobias.playpad.viewcontroller.settings.WarningFeedbackViewController;
import de.tobias.utils.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

public class PlayerTabViewController extends ProfileSettingsTabViewController {

	// Modus
	@FXML private CheckBox playerModus;

	// Player
	@FXML private VBox warningFeedbackContainer;
	@FXML private VBox fadeContainer;
	@FXML private ComboBox<TimeMode> timeDisplayComboBox;

	PlayerTabViewController() {
		load("de/tobias/playpad/assets/view/option/profile/", "playerTab", PlayPadMain.getUiResourceBundle());

		// Player
		FadeViewController fadeViewController = new FadeViewController();
		fadeViewController.setFade(Profile.currentProfile().getProfileSettings().getFade());
		fadeContainer.getChildren().add(fadeViewController.getParent());
		setAnchor(fadeViewController.getParent(), 0, 0, 0, 0);
	}

	public ComboBox<TimeMode> getTimeDisplayComboBox() {
		return timeDisplayComboBox;
	}

	@Override
	public void init() {
		WarningFeedbackViewController controller = new WarningFeedbackViewController();
		warningFeedbackContainer.getChildren().add(controller.getParent());

		// Player
		timeDisplayComboBox.getItems().addAll(TimeMode.values());
		timeDisplayComboBox.setButtonCell(new EnumCell<>(Strings.Pad_TimeMode_BaseName));
		timeDisplayComboBox.setCellFactory(list -> new EnumCell<>(Strings.Pad_TimeMode_BaseName));
	}

	@Override
	public void loadSettings(Profile profile) {
		ProfileSettings profileSettings = profile.getProfileSettings();

		playerModus.setSelected(profile.getProfileSettings().isMultiplePlayer());
		timeDisplayComboBox.setValue(profileSettings.getPlayerTimeDisplayMode());
	}

	@Override
	public void saveSettings(Profile profile) {
		ProfileSettings profileSettings = profile.getProfileSettings();

		// Player
		profileSettings.setMultiplePlayer(playerModus.isSelected());
		profileSettings.setPlayerTimeDisplayMode(timeDisplayComboBox.getValue());
	}

	@Override
	public boolean needReload() {
		return false;
	}

	@Override
	public boolean validSettings() {
		return true;
	}

	@Override
	public String name() {
		return Localization.getString(Strings.UI_Window_Settings_Player_Title);
	}
}
