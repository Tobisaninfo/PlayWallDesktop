package de.tobias.playpad.viewcontroller.settings;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;
import de.tobias.playpad.settings.FadeSettings;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.util.Duration;

public class FadeSettingsViewController extends NVC {

	@FXML
	private Slider fadeInSlider;
	@FXML
	private Slider fadeOutSlider;
	@FXML
	private Label fadeInLabel;
	@FXML
	private Label fadeOutLabel;

	@FXML
	private CheckBox fadeInStartCheckBox;
	@FXML
	private CheckBox fadeInPauseCheckBox;
	@FXML
	private CheckBox fadeOutPauseCheckBox;
	@FXML
	private CheckBox fadeOutStopCheckBox;
	@FXML
	private CheckBox fadeOutEofCheckBox;

	private FadeSettings fade;

	public FadeSettingsViewController() {
		load("view/settings", "FadeView", Localization.getBundle());
	}

	@Override
	public void init() {
		fadeInSlider.valueProperty().addListener((a, b, c) ->
		{
			Duration seconds = Duration.seconds(c.doubleValue());
			double displayedTime = Math.round(seconds.toSeconds() * 10) / 10.0;
			fadeInLabel.setText(Localization.getString(Strings.STANDARD_TIME_SECONDS, displayedTime));
			fade.setFadeIn(seconds);
		});

		fadeOutSlider.valueProperty().addListener((a, b, c) ->
		{
			Duration seconds = Duration.seconds(c.doubleValue());
			double displayedTime = Math.round(seconds.toSeconds() * 10) / 10.0;
			fadeOutLabel.setText(Localization.getString(Strings.STANDARD_TIME_SECONDS, displayedTime));
			fade.setFadeOut(seconds);
		});

		fadeInStartCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> fade.setFadeInStart(newValue));
		fadeInPauseCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> fade.setFadeInPause(newValue));
		fadeOutPauseCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> fade.setFadeOutPause(newValue));
		fadeOutStopCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> fade.setFadeOutStop(newValue));
		fadeOutEofCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> fade.setFadeOutEof(newValue));
	}

	public void setFadeSettings(FadeSettings fade) {
		this.fade = fade;

		if (fade != null) {
			fadeInSlider.setValue(fade.getFadeIn().toSeconds());
			double displayedInTime = Math.round(fade.getFadeIn().toSeconds() * 10) / 10.0;
			fadeInLabel.setText(Localization.getString(Strings.STANDARD_TIME_SECONDS, displayedInTime));

			fadeOutSlider.setValue(fade.getFadeOut().toSeconds());
			double displayedOutTime = Math.round(fade.getFadeOut().toSeconds() * 10) / 10.0;
			fadeOutLabel.setText(Localization.getString(Strings.STANDARD_TIME_SECONDS, displayedOutTime));

			fadeInStartCheckBox.setSelected(fade.isFadeInStart());
			fadeInPauseCheckBox.setSelected(fade.isFadeInPause());
			fadeOutPauseCheckBox.setSelected(fade.isFadeOutPause());
			fadeOutStopCheckBox.setSelected(fade.isFadeOutStop());
			fadeOutEofCheckBox.setSelected(fade.isFadeOutEof());
		}
	}
}
