package de.tobias.playpad.viewcontroller.option.pad;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.TimeMode;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.cell.EnumCell;
import de.tobias.utils.util.Localization;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class GeneralPadTabViewController extends PadSettingsTabViewController {

	@FXML private TextField titleTextField;
	@FXML private Slider volumeSlider;
	@FXML private CheckBox repeatCheckBox;

	@FXML private CheckBox customTimeDisplayCheckBox;
	@FXML private ComboBox<TimeMode> timeDisplayComboBox;

	@FXML private Button deleteButton;

	private Pad pad;

	private ChangeListener<Number> volumeListener;

	public GeneralPadTabViewController(Pad pad) {
		super("generalTab", "de/tobias/playpad/assets/view/option/pad/", PlayPadMain.getUiResourceBundle());
		this.pad = pad;
		
		if (pad.getStatus() == PadStatus.PLAY || pad.getStatus() == PadStatus.PAUSE) {
			deleteButton.setDisable(true);
		}
	}

	@Override
	public void init() {
		// Init Listener
		volumeListener = (a, b, c) -> pad.setVolume(c.doubleValue() / 100.0);

		volumeSlider.valueProperty().addListener(volumeListener);

		customTimeDisplayCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			timeDisplayComboBox.setDisable(!c);
			if (c && !pad.isCustomTimeMode())
				pad.setTimeMode(TimeMode.REST);
			else if (b && pad.isCustomTimeMode())
				pad.setTimeMode(null);

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
		// Bindings
		titleTextField.textProperty().bindBidirectional(pad.nameProperty());
		repeatCheckBox.selectedProperty().bindBidirectional(pad.loopProperty());
		timeDisplayComboBox.valueProperty().bindBidirectional(pad.timeModeProperty());

		volumeSlider.setValue(pad.getVolume() * 100);

		// is Custom TimeMode Actvie
		customTimeDisplayCheckBox.setSelected(pad.isCustomTimeMode());
		if (!pad.isCustomTimeMode()) {
			timeDisplayComboBox.setDisable(true);
		}
	}

	@Override
	public void saveSettings(Pad pad) {
		titleTextField.textProperty().unbindBidirectional(pad.nameProperty());
		repeatCheckBox.selectedProperty().unbindBidirectional(pad.loopProperty());
		timeDisplayComboBox.valueProperty().unbindBidirectional(pad.timeModeProperty());
	}

	// Listener
	@FXML
	private void deleteButtonHandler(ActionEvent event) {
		pad.clear();
		((Stage) getStage()).close();
	}
}
