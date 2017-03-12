package de.tobias.playpad.viewcontroller.dialog;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import de.tobias.playpad.settings.Profile;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Worker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.sound.midi.MidiDevice.Info;
import java.util.List;

public class NewProfileDialog extends NVC {

	@FXML private TextField nameTextField;
	@FXML private CheckBox activeCheckBox;
	@FXML private ComboBox<String> midiDeviceComboBox;

	@FXML private Button finishButton;
	@FXML private Button cancelButton;

	@FXML private VBox accordionParent;
	@FXML private Accordion accordion;

	private Profile profile;

	NewProfileDialog(Window owner) {
		load("de/tobias/playpad/assets/dialog/", "newProfileDialog", PlayPadMain.getUiResourceBundle());

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);
		nvcStage.getStage().sizeToScene();
		addCloseKeyShortcut(() -> getStageContainer().ifPresent(NVCStage::close));

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
	}

	private boolean expand = false;

	@Override
	public void init() {
		accordion.heightProperty().addListener((obs, oldHeight, newHeight) ->
		{
			if (newHeight.doubleValue() > oldHeight.doubleValue()) {
				if (accordionParent.getHeight() <= newHeight.doubleValue()) {
					getStageContainer().ifPresent(nvcStage -> {
						Stage stage = nvcStage.getStage();
						stage.setHeight(stage.getHeight() + newHeight.doubleValue());
					});
					expand = true;
				}
			} else {
				if (expand) {
					getStageContainer().ifPresent(nvcStage -> {
						Stage stage = nvcStage.getStage();
						stage.setHeight(stage.getHeight() - oldHeight.doubleValue());
					});
				}
				expand = false;
			}
		});

		nameTextField.textProperty().addListener((a, b, c) ->
		{
			if (c.isEmpty()) {
				finishButton.setDisable(true);
			} else {
				if (ProfileReferenceManager.getProfiles().contains(c) || !c.matches(Profile.profileNameEx)) {
					finishButton.setDisable(true);
					return;
				}
				finishButton.setDisable(false);
			}
		});
		finishButton.setDisable(true);
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setTitle(Localization.getString(Strings.UI_Dialog_NewProfile_Title));
		stage.setWidth(400);
		stage.setHeight(200);

		stage.setMinWidth(500);
		stage.setMinHeight(200);

		stage.setMaxWidth(500);

		stage.initModality(Modality.WINDOW_MODAL);

		if (Profile.currentProfile() != null) {
			Profile.currentProfile().currentLayout().applyCss(stage);
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
			List<ProfileReference> profiles = ProfileReferenceManager.getProfiles();

			if (profiles.contains(name) || !name.matches(Profile.profileNameEx)) {
				showErrorMessage(Localization.getString(Strings.Error_Standard_NameInUse, name));
				return;
			}

			profile = ProfileReferenceManager.newProfile(name);

			profile.getProfileSettings().setMidiActive(activeCheckBox.isSelected());
			profile.getProfileSettings().setMidiDeviceName(midiDeviceComboBox.getSelectionModel().getSelectedItem());

			profile.save();
			getStageContainer().ifPresent(NVCStage::close);
		} catch (Exception e) {
			e.printStackTrace();
			showErrorMessage(Localization.getString(Strings.Error_Profile_Create, e.getMessage()));
		}
	}

	@FXML
	private void cancelButtonHandler(ActionEvent event) {
		getStageContainer().ifPresent(NVCStage::close);
	}

	public Profile getProfile() {
		return profile;
	}
}
