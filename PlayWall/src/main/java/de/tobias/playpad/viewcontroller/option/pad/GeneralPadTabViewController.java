package de.tobias.playpad.viewcontroller.option.pad;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.TimeMode;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.cell.EnumCell;
import de.tobias.utils.util.Localization;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class GeneralPadTabViewController extends PadSettingsTabViewController {

	@FXML
	private TextField titleTextField;
	@FXML
	private Slider volumeSlider;
	@FXML
	private CheckBox repeatCheckBox;

	@FXML
	private CheckBox customTimeDisplayCheckBox;
	@FXML
	private ComboBox<TimeMode> timeDisplayComboBox;

	@FXML
	private Button deleteButton;

	private Pad pad;

	GeneralPadTabViewController(Pad pad) {
		load("view/option/pad", "GeneralTab", PlayPadMain.getUiResourceBundle());
		this.pad = pad;

		if (pad.getStatus() == PadStatus.PLAY || pad.getStatus() == PadStatus.PAUSE) {
			deleteButton.setDisable(true);
		}
	}

	@Override
	public void init() {
		// Init Listener
		ChangeListener<Number> volumeListener = (a, b, c) -> pad.getPadSettings().setVolume(c.doubleValue() / 100.0);
		volumeSlider.valueProperty().addListener(volumeListener);

		customTimeDisplayCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			PadSettings padSettings = pad.getPadSettings();

			timeDisplayComboBox.setDisable(!c);
			if (c && !padSettings.isCustomTimeMode())
				padSettings.setTimeMode(TimeMode.REST);
			else if (b && padSettings.isCustomTimeMode())
				padSettings.setTimeMode(null);

		});
		timeDisplayComboBox.getItems().addAll(TimeMode.values());
		timeDisplayComboBox.setButtonCell(new EnumCell<>(Strings.Pad_TimeMode_BaseName));
		timeDisplayComboBox.setCellFactory(list -> new EnumCell<>(Strings.Pad_TimeMode_BaseName));
	}

	@Override
	public String getName() {
		return Localization.getString(Strings.UI_Window_PadSettings_General_Title);
	}

	@Override
	public void loadSettings(Pad pad) {
		PadSettings padSettings = pad.getPadSettings();

		// Bindings
		titleTextField.textProperty().bindBidirectional(pad.nameProperty());
		repeatCheckBox.selectedProperty().bindBidirectional(padSettings.loopProperty());
		timeDisplayComboBox.valueProperty().bindBidirectional(padSettings.timeModeProperty());

		volumeSlider.setValue(padSettings.getVolume() * 100);

		// is Custom TimeMode Actvie
		customTimeDisplayCheckBox.setSelected(padSettings.isCustomTimeMode());
		if (!padSettings.isCustomTimeMode()) {
			timeDisplayComboBox.setDisable(true);
		}
	}

	@Override
	public void saveSettings(Pad pad) {
		PadSettings padSettings = pad.getPadSettings();

		titleTextField.textProperty().unbindBidirectional(pad.nameProperty());
		repeatCheckBox.selectedProperty().unbindBidirectional(padSettings.loopProperty());
		timeDisplayComboBox.valueProperty().unbindBidirectional(padSettings.timeModeProperty());
	}

	// Listener
	@FXML
	private void deleteButtonHandler(ActionEvent event) {
		pad.clear();
		((Stage) getContainingWindow()).close();
	}
}
