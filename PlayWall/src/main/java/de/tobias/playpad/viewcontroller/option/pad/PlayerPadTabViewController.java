package de.tobias.playpad.viewcontroller.option.pad;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.settings.FadeSettings;
import de.tobias.playpad.view.PseudoClasses;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.settings.FadeSettingsViewController;
import de.tobias.playpad.viewcontroller.settings.WarningFeedbackViewController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class PlayerPadTabViewController extends PadSettingsTabViewController {

	@FXML
	private CheckBox playOverlayEnableCheckBox;
	@FXML
	private CheckBox customFadeCheckBox;
	@FXML
	private VBox fadeContainer;
	private FadeSettingsViewController fadeViewController;

	@FXML
	private VBox warningFeedbackContainer;
	@FXML
	private CheckBox warningEnableCheckBox;
	private WarningFeedbackViewController warningFeedbackViewController;

	@FXML
	private TextField cueInTextField;

	private Pad pad;

	PlayerPadTabViewController(Pad pad) {
		load("view/option/pad", "PlayerTab", Localization.getBundle());
		this.pad = pad;
	}

	@Override
	public void init() {
		// Embed ViewController
		fadeViewController = new FadeSettingsViewController();
		fadeContainer.getChildren().add(fadeViewController.getParent());

		warningFeedbackViewController = WarningFeedbackViewController.newViewControllerForPad();
		warningFeedbackContainer.getChildren().add(warningFeedbackViewController.getParent());

		playOverlayEnableCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> pad.getPadSettings().setPlayOverlay(newValue));

		customFadeCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			fadeContainer.setDisable(!c);
			PadSettings padSettings = pad.getPadSettings();

			if (c && !padSettings.isCustomFade())
				padSettings.setFade(new FadeSettings());
			else if (!c && padSettings.isCustomFade())
				padSettings.setFade(null);

			if (c)
				fadeViewController.setFadeSettings(padSettings.getFade());
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

		cueInTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			PadSettings padSettings = pad.getPadSettings();
			if (newValue.isEmpty()) {
				padSettings.setCueIn(null);
				cueInTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, false);
			} else {
				try {
					final double seconds = Double.parseDouble(newValue);
					padSettings.setCueIn(Duration.seconds(seconds));

					cueInTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, false);
				} catch (NumberFormatException e) {
					cueInTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true);
				}
			}
		});
	}

	@Override
	public String getName() {
		return Localization.getString(Strings.UI_WINDOW_PAD_SETTINGS_PLAYER_TITLE);
	}

	@Override
	public void loadSettings(Pad pad) {
		PadSettings padSettings = pad.getPadSettings();

		playOverlayEnableCheckBox.setSelected(padSettings.isPlayOverlay());

		if (padSettings.isCustomFade())
			fadeViewController.setFadeSettings(padSettings.getFade());

		customFadeCheckBox.setSelected(padSettings.isCustomFade());
		if (!padSettings.isCustomFade()) {
			fadeContainer.setDisable(true);
		}

		warningEnableCheckBox.setSelected(padSettings.isCustomWarning());
		if (!padSettings.isCustomWarning()) {
			warningFeedbackContainer.setDisable(true);
		}

		final Duration cueIn = padSettings.getCueIn();
		if (cueIn != null) {
			cueInTextField.setText(String.valueOf(cueIn.toSeconds()));
		}
	}

	@Override
	public void saveSettings(Pad pad) {

	}
}
