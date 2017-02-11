package de.tobias.playpad.viewcontroller.option.pad.trigger;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.trigger.VolumeTriggerItem;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.util.Duration;

public class VolumeTriggerViewController extends NVC {

	@FXML private Slider volumeSlider;
	@FXML private Label volumeLabel;

	@FXML private Slider durationSlider;
	@FXML private Label durationLabel;

	private VolumeTriggerItem item;

	public VolumeTriggerViewController(VolumeTriggerItem item) {
		load("de/tobias/playpad/assets/view/option/pad/trigger/", "volumeTrigger", PlayPadMain.getUiResourceBundle());
		this.item = item;

		volumeSlider.setValue(item.getVolume() * 100.0);
		durationSlider.setValue(item.getDuration().toSeconds());

	}

	@Override
	public void init() {
		volumeSlider.valueProperty().addListener((a, b, c) ->
		{
			item.setVolume(c.doubleValue() / 100.0);
			volumeLabel.setText(Localization.getString(Strings.Standard_Time_Volume, Math.round(c.doubleValue())));
		});

		durationSlider.valueProperty().addListener((a, b, c) ->
		{
			item.setDuration(Duration.seconds(c.doubleValue()));

			double secounds = Math.round(item.getDuration().toSeconds() * 10.0) / 10.0;
			durationLabel.setText(Localization.getString(Strings.Standard_Time_Seconds, secounds));
		});

	}
}
