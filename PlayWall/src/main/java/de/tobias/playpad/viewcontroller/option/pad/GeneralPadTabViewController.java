package de.tobias.playpad.viewcontroller.option.pad;

import de.thecodelabs.utils.application.system.NativeApplication;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;
import de.tobias.playpad.layout.desktop.listener.PadNewContentListener;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.TimeMode;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.pad.content.Playlistable;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.cell.EnumCell;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

public class GeneralPadTabViewController extends PadSettingsTabViewController {

	@FXML
	private VBox mediaRootBox;
	@FXML
	private Label pathLabel;
	@FXML
	private Button showPathButton;

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

	private final Pad pad;

	GeneralPadTabViewController(Pad pad) {
		load("view/option/pad", "GeneralTab", Localization.getBundle());
		this.pad = pad;

		if (pad.getPath() != null) {
			pathLabel.setText(pad.getPath().toString());
		} else {
			pathLabel.setText(Localization.getString("padSettings.gen.label.media.empty"));
		}
		showPathButton.disableProperty().bind(Bindings.isEmpty(pad.getPaths()));

		// Disable media section for playlists
		if (pad.getContent() instanceof Playlistable) {
			mediaRootBox.setDisable(true);
		}
		pathLabel.setText(pad.getPath().toString());

		if (pad.getStatus() == PadStatus.PLAY || pad.getStatus() == PadStatus.PAUSE) {
			deleteButton.setDisable(true);
		}
	}

	@Override
	public void init() {
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
		timeDisplayComboBox.setButtonCell(new EnumCell<>(Strings.PAD_TIME_MODE));
		timeDisplayComboBox.setCellFactory(list -> new EnumCell<>(Strings.PAD_TIME_MODE));
	}

	@Override
	public String getName() {
		return Localization.getString(Strings.UI_WINDOW_PAD_SETTINGS_GENERAL_TITLE);
	}

	@Override
	public void loadSettings(Pad pad) {
		final PadSettings padSettings = pad.getPadSettings();

		titleTextField.textProperty().bindBidirectional(pad.nameProperty());
		repeatCheckBox.selectedProperty().bindBidirectional(padSettings.loopProperty());
		timeDisplayComboBox.valueProperty().bindBidirectional(padSettings.timeModeProperty());

		volumeSlider.setValue(padSettings.getVolume() * 100);

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

	@FXML
	private void showPathButtonHandler() {
		NativeApplication.sharedInstance().showFileInFileViewer(pad.getPath());
	}

	@FXML
	private void chooseButtonHandler(ActionEvent event) {
		final PadNewContentListener listener = new PadNewContentListener(pad);
		listener.onNew(event, (options, onSelected) -> {
			ChoiceDialog<PadContentFactory> dialog = new ChoiceDialog<>(null, options);
			final Optional<PadContentFactory> padContentFactory = dialog.showAndWait();
			padContentFactory.ifPresent(onSelected);
		});
	}

	@FXML
	private void deleteButtonHandler(ActionEvent event) {
		pad.clear();
		((Stage) getContainingWindow()).close();
	}
}
