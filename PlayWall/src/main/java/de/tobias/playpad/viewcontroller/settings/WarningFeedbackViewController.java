package de.tobias.playpad.viewcontroller.settings;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.utils.ui.NVC;
import de.tobias.utils.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.util.Duration;

public class WarningFeedbackViewController extends NVC {

	@FXML
	private Slider warningFeedbackTimeSlider;
	@FXML
	private Label warningFeedbackTimeLabel;

	public WarningFeedbackViewController() {
		load("view/settings", "WarningFeedbackSettingsView", PlayPadMain.getUiResourceBundle());
		ProfileSettings profilSettings = Profile.currentProfile().getProfileSettings();

		warningFeedbackTimeSlider.setValue(profilSettings.getWarningFeedback().toSeconds());
		setTimeLabel();

		warningFeedbackTimeSlider.valueProperty().addListener((a, b, c) ->
		{
			profilSettings.setWarningTime(Duration.seconds(c.doubleValue()));
		});
	}

	public WarningFeedbackViewController(Pad pad) {
		load("view/settings/", "WarningFeedbackSettingsView", PlayPadMain.getUiResourceBundle());
	}

	@Override
	public void init() {
		warningFeedbackTimeSlider.valueProperty().addListener((a, b, c) ->
		{
			setTimeLabel();
		});
	}

	private void setTimeLabel() {
		double displayedTime = Math.round(warningFeedbackTimeSlider.getValue() * 10) / 10.0;
		warningFeedbackTimeLabel.setText(Localization.getString(Strings.Standard_Time_Seconds, displayedTime));
	}

	public void setPadWarning(Pad pad) {
		if (pad.getPadSettings().getWarning() != null) {
			warningFeedbackTimeSlider.setValue(pad.getPadSettings().getWarning().toSeconds());
			setTimeLabel();
		}

		warningFeedbackTimeSlider.valueProperty().addListener((a, b, c) ->
		{
			pad.getPadSettings().setWarning(Duration.seconds(c.doubleValue()));
		});
	}
}
