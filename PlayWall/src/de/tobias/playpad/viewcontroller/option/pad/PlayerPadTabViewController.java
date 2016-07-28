package de.tobias.playpad.viewcontroller.option.pad;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.settings.Fade;
import de.tobias.playpad.settings.Warning;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.settings.FadeViewController;
import de.tobias.playpad.viewcontroller.settings.WarningFeedbackViewController;
import de.tobias.utils.util.Localization;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

public class PlayerPadTabViewController extends PadSettingsTabViewController {

	@FXML private CheckBox customFadeCheckBox;
	@FXML private VBox fadeContainer;
	private FadeViewController fadeViewController;

	@FXML private VBox warningFeedbackContainer;
	@FXML private CheckBox warningEnableCheckBox;
	private WarningFeedbackViewController warningFeedbackViewController;

	private Pad pad;

	public PlayerPadTabViewController(Pad pad) {
		super("playerTab", "de/tobias/playpad/assets/view/option/pad/", PlayPadMain.getUiResourceBundle());
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
			if (c && !pad.isCustomFade())
				pad.setFade(new Fade());
			else if (!c && pad.isCustomFade())
				pad.setFade(null);

			if (c)
				fadeViewController.setFade(pad.getFade());
		});

		warningEnableCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			warningFeedbackContainer.setDisable(!c);
			if (c && !pad.isCustomWarning())
				pad.setWarning(new Warning());
			else if (!c && pad.isCustomWarning())
				pad.setWarning(null);

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
		if (pad.isCustomFade())
			fadeViewController.setFade(pad.getFade());

		customFadeCheckBox.setSelected(pad.isCustomFade());
		if (!pad.isCustomFade()) {
			fadeContainer.setDisable(true);
		}

		warningEnableCheckBox.setSelected(pad.isCustomWarning());
		if (!pad.isCustomWarning()) {
			warningFeedbackContainer.setDisable(true);
		}
	}

	@Override
	public void saveSettings(Pad pad) {

	}
}
