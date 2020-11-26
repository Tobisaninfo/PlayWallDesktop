package de.tobias.playpad.viewcontroller.option.profile;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.TimeMode;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.playpad.viewcontroller.cell.EnumCell;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import de.tobias.playpad.viewcontroller.settings.FadeViewController;
import de.tobias.playpad.viewcontroller.settings.WarningFeedbackViewController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

public class PlayerTabViewController extends ProfileSettingsTabViewController {

	// Modus
	@FXML
	private CheckBox playerModus;

	// Player
	@FXML
	private VBox warningFeedbackContainer;
	@FXML
	private VBox fadeContainer;
	@FXML
	private ComboBox<TimeMode> timeDisplayComboBox;

	PlayerTabViewController() {
		load("view/option/profile", "PlayerTab", Localization.getBundle());

		// Player
		FadeViewController fadeViewController = new FadeViewController();
		fadeViewController.setFadeSettings(Profile.currentProfile().getProfileSettings().getFade());
		fadeContainer.getChildren().add(fadeViewController.getParent());
		setAnchor(fadeViewController.getParent(), 0, 0, 0, 0);
	}

	public ComboBox<TimeMode> getTimeDisplayComboBox() {
		return timeDisplayComboBox;
	}

	@Override
	public void init() {
		WarningFeedbackViewController controller = WarningFeedbackViewController.newViewControllerForProfile();
		warningFeedbackContainer.getChildren().add(controller.getParent());

		// Player
		timeDisplayComboBox.getItems().addAll(TimeMode.values());
		timeDisplayComboBox.setButtonCell(new EnumCell<>(Strings.PAD_TIME_MODE));
		timeDisplayComboBox.setCellFactory(list -> new EnumCell<>(Strings.PAD_TIME_MODE));
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
		return Localization.getString(Strings.UI_WINDOW_SETTINGS_PLAYER_TITLE);
	}
}
