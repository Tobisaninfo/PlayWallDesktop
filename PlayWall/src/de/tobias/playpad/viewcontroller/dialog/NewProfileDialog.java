package de.tobias.playpad.viewcontroller.dialog;

import javax.sound.midi.MidiDevice.Info;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileReference;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Worker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class NewProfileDialog extends ViewController {

	@FXML private TextField nameTextField;
	@FXML private CheckBox activeCheckBox;
	@FXML private ComboBox<String> midiDeviceComboBox;

	@FXML private Button finishButton;
	@FXML private Button cancelButton;

	@FXML private VBox accordionParent;
	@FXML private Accordion accordion;

	private Profile profile;

	public NewProfileDialog(Window owner) {
		super("newProfileDialog", "de/tobias/playpad/assets/dialog/", null, PlayPadMain.getUiResourceBundle());

		getStage().initOwner(owner);
		getStage().initModality(Modality.WINDOW_MODAL);

		midiDeviceComboBox.setDisable(!activeCheckBox.isSelected());

		// In Worker, da Midi.getMidiDevices etwas dauert und FX-Thread sonst blockiert
		Worker.runLater(() ->
		{
			Info[] data = Midi.getMidiDevices();

			// Gerät anzeigen - Doppelte weg
			Platform.runLater(() ->
			{
				for (Info item : data) {
					if (!midiDeviceComboBox.getItems().contains(item.getName())) {
						midiDeviceComboBox.getItems().add(item.getName());
					}
				}
			});
		});
		getStage().sizeToScene();
	}

	private boolean expand = false;

	@Override
	public void init() {
		accordion.heightProperty().addListener((obs, oldHeight, newHeight) ->
		{
			if (newHeight.doubleValue() > oldHeight.doubleValue()) {
				if (accordionParent.getHeight() <= newHeight.doubleValue()) {
					getStage().setHeight(getStage().getHeight() + newHeight.doubleValue());
					expand = true;
				}
			} else {
				if (expand)
					getStage().setHeight(getStage().getHeight() - oldHeight.doubleValue());
				expand = false;
			}
		});

		nameTextField.textProperty().addListener((a, b, c) ->
		{
			if (c.isEmpty()) {
				finishButton.setDisable(true);
			} else {
				if (ProfileReference.getProfiles().contains(c) || !c.matches(Profile.profileNameEx)) {
					finishButton.setDisable(true);
					return;
				}
				finishButton.setDisable(false);
			}
		});
		finishButton.setDisable(true);

		addCloseKeyShortcut(() -> getStage().close());
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setTitle(Localization.getString(Strings.UI_Dialog_NewProile_Title));
		stage.setWidth(400);
		stage.setHeight(200);

		stage.setMinWidth(500);
		stage.setMinHeight(200);

		stage.setMaxWidth(500);

		if (Profile.currentProfile() != null) {
			Profile.currentProfile().currentLayout().applyCss(getStage());
		}
	}

	@FXML
	private void activeCheckBoxHandler(ActionEvent actionEvent) {
		midiDeviceComboBox.setDisable(!activeCheckBox.isSelected());
	}

	@FXML
	private void finishButtonHandler(ActionEvent event) {
		String name = nameTextField.getText();
		try {
			if (ProfileReference.getProfiles().contains(name) || !name.matches(Profile.profileNameEx)) {
				showErrorMessage(Localization.getString(Strings.Error_Standard_NameInUse, name));
				return;
			}

			profile = ProfileReference.newProfile(name);

			profile.getProfileSettings().setMidiActive(activeCheckBox.isSelected());
			profile.getProfileSettings().setMidiDeviceName(midiDeviceComboBox.getSelectionModel().getSelectedItem());

			profile.save();
			getStage().close();
		} catch (Exception e) {
			e.printStackTrace();
			showErrorMessage(Localization.getString(Strings.Error_Profile_Create, e.getMessage()));
		}
	}

	@FXML
	private void cancelButtonHandler(ActionEvent event) {
		getStage().close();
	}

	public Profile getProfile() {
		return profile;
	}
}
