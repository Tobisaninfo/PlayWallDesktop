package de.tobias.playpad.viewcontroller.settings;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Fade;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.util.Duration;

public class FadeViewController extends ContentViewController {

	@FXML private Slider fadeInSlider;
	@FXML private Slider fadeOutSlider;
	@FXML private Label fadeInLabel;
	@FXML private Label fadeOutLabel;

	@FXML private CheckBox fadeInStartCheckBox;
	@FXML private CheckBox fadeInPauseCheckBox;
	@FXML private CheckBox fadeOutPauseCheckBox;
	@FXML private CheckBox fadeOutStopCheckBox;

	private Fade fade;

	public FadeViewController() {
		super("fadeView", "de/tobias/playpad/assets/settings/", PlayPadMain.getUiResourceBundle());
	}

	@Override
	public void init() {
		fadeInSlider.valueProperty().addListener((a, b, c) ->
		{
			Duration seconds = Duration.seconds(c.doubleValue());
			double displayedTime = Math.round(seconds.toSeconds() * 10) / 10.0;
			fadeInLabel.setText(Localization.getString(Strings.Standard_Time_Seconds, displayedTime));
			fade.setFadeIn(seconds);
		});

		fadeOutSlider.valueProperty().addListener((a, b, c) ->
		{
			Duration seconds = Duration.seconds(c.doubleValue());
			double displayedTime = Math.round(seconds.toSeconds() * 10) / 10.0;
			fadeOutLabel.setText(Localization.getString(Strings.Standard_Time_Seconds, displayedTime));
			fade.setFadeOut(seconds);
		});

		fadeInStartCheckBox.selectedProperty().addListener((a, b, c) -> fade.setFadeInStart(c));
		fadeInPauseCheckBox.selectedProperty().addListener((a, b, c) -> fade.setFadeInPause(c));
		fadeOutPauseCheckBox.selectedProperty().addListener((a, b, c) -> fade.setFadeOutPause(c));
		fadeOutStopCheckBox.selectedProperty().addListener((a, b, c) -> fade.setFadeOutStop(c));
	}

	public void setFade(Fade fade) {
		this.fade = fade;

		if (fade != null) {
			fadeInSlider.setValue(fade.getFadeIn().toSeconds());
			double displayedInTime = Math.round(fade.getFadeIn().toSeconds() * 10) / 10.0;
			fadeInLabel.setText(Localization.getString(Strings.Standard_Time_Seconds, displayedInTime));
			
			fadeOutSlider.setValue(fade.getFadeOut().toSeconds());
			double displayedOutTime = Math.round(fade.getFadeOut().toSeconds() * 10) / 10.0;
			fadeOutLabel.setText(Localization.getString(Strings.Standard_Time_Seconds, displayedOutTime));
			
			fadeInStartCheckBox.setSelected(fade.isFadeInStart());
			fadeInPauseCheckBox.setSelected(fade.isFadeInPause());
			fadeOutPauseCheckBox.setSelected(fade.isFadeOutPause());
			fadeOutStopCheckBox.setSelected(fade.isFadeOutStop());
		}
	}
}
