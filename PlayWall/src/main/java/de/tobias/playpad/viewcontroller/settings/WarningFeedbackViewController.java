package de.tobias.playpad.viewcontroller.settings;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileSettings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.util.Duration;

public class WarningFeedbackViewController extends NVC {

	@FXML
	private Slider warningFeedbackTimeSlider;
	@FXML
	private Label warningFeedbackTimeLabel;

	private WarningFeedbackViewController() {
		load("view/settings", "WarningFeedbackSettingsView", Localization.getBundle());
	}

	public static WarningFeedbackViewController newViewControllerForProfile() {
		final WarningFeedbackViewController controller = new WarningFeedbackViewController();
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();

		controller.warningFeedbackTimeSlider.setValue(profileSettings.getWarningFeedback().toSeconds());
		controller.setTimeLabel();

		controller.warningFeedbackTimeSlider.valueProperty().addListener((a, b, c) ->
				profileSettings.setWarningTime(Duration.seconds(c.doubleValue())));
		return controller;
	}

	public static WarningFeedbackViewController newViewControllerForPad() {
		return new WarningFeedbackViewController();
	}

	@Override
	public void init() {
		warningFeedbackTimeSlider.valueProperty().addListener((a, b, c) ->
				setTimeLabel());
	}

	private void setTimeLabel() {
		double displayedTime = Math.round(warningFeedbackTimeSlider.getValue() * 10) / 10.0;
		warningFeedbackTimeLabel.setText(Localization.getString(Strings.STANDARD_TIME_SECONDS, displayedTime));
	}

	public void setPadWarning(Pad pad) {
		if (pad.getPadSettings().getWarning() != null) {
			warningFeedbackTimeSlider.setValue(pad.getPadSettings().getWarning().toSeconds());
			setTimeLabel();
		}

		warningFeedbackTimeSlider.valueProperty().addListener((a, b, c) ->
				pad.getPadSettings().setWarning(Duration.seconds(c.doubleValue())));
	}
}
