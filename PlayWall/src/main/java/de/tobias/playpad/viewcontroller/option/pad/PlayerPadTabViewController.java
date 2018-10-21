package de.tobias.playpad.viewcontroller.option.pad;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.settings.Fade;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.settings.FadeViewController;
import de.tobias.playpad.viewcontroller.settings.WarningFeedbackViewController;
import de.tobias.utils.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class PlayerPadTabViewController extends PadSettingsTabViewController {

	@FXML
	private CheckBox customFadeCheckBox;
	@FXML
	private VBox fadeContainer;
	private FadeViewController fadeViewController;

	@FXML
	private VBox warningFeedbackContainer;
	@FXML
	private CheckBox warningEnableCheckBox;
	private WarningFeedbackViewController warningFeedbackViewController;

	private Pad pad;

	PlayerPadTabViewController(Pad pad) {
		load("view/option/pad", "PlayerTab", PlayPadMain.getUiResourceBundle());
		this.pad = pad;
	}

	@Override
	public void init() {
		// Embed ViewController
		fadeViewController = new FadeViewController();
		fadeContainer.getChildren().add(fadeViewController.getParent());

		warningFeedbackViewController = new WarningFeedbackViewController(null);
		warningFeedbackContainer.getChildren().add(warningFeedbackViewController.getParent());

		customFadeCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			fadeContainer.setDisable(!c);
			PadSettings padSettings = pad.getPadSettings();

			if (c && !padSettings.isCustomFade())
				padSettings.setFade(new Fade());
			else if (!c && padSettings.isCustomFade())
				padSettings.setFade(null);

			if (c)
				fadeViewController.setFade(padSettings.getFade());
		});

		warningEnableCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			warningFeedbackContainer.setDisable(!c);
			PadSettings padSettings = pad.getPadSettings();

			if (c && !padSettings.isCustomWarning())
				padSettings.setWarning(Duration.seconds(5));
			else if (!c && padSettings.isCustomWarning())
				padSettings.setWarning(null);

			if (c)
				warningFeedbackViewController.setPadWarning(pad);
		});
	}

	@Override
	public String getName() {
		return Localization.getString(Strings.UI_Window_PadSettings_Player_Title);
	}

	@Override
	public void loadSettings(Pad pad) {
		PadSettings padSettings = pad.getPadSettings();

		if (padSettings.isCustomFade())
			fadeViewController.setFade(padSettings.getFade());

		customFadeCheckBox.setSelected(padSettings.isCustomFade());
		if (!padSettings.isCustomFade()) {
			fadeContainer.setDisable(true);
		}

		warningEnableCheckBox.setSelected(padSettings.isCustomWarning());
		if (!padSettings.isCustomWarning()) {
			warningFeedbackContainer.setDisable(true);
		}
	}

	@Override
	public void saveSettings(Pad pad) {

	}
}
