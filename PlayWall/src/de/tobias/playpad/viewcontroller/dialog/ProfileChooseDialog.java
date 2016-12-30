package de.tobias.playpad.viewcontroller.dialog;

import java.io.IOException;

import org.dom4j.DocumentException;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferences;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ProfileChooseDialog extends ViewController {

	@FXML private ComboBox<ProfileReference> profileComboBox;
	@FXML private Button newProfileButton;

	@FXML private Button finishButton;
	@FXML private Button cancelButton;

	private Profile profile;

	public ProfileChooseDialog(Window owner) {
		super("profileChooseDialog", "de/tobias/playpad/assets/dialog/", null, PlayPadMain.getUiResourceBundle());

		getStage().initOwner(owner);
		getStage().initModality(Modality.WINDOW_MODAL);

		profileComboBox.getItems().addAll(ProfileReferences.getProfiles());
		profileComboBox.getSelectionModel().selectFirst();
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setTitle(Localization.getString(Strings.UI_Dialog_ChooseProfile_Title));
		stage.setWidth(560);
		stage.setHeight(180);
		stage.setMinWidth(560);
		stage.setMinHeight(180);
		stage.setMaxWidth(560);

		if (Profile.currentProfile() != null) {
			Profile.currentProfile().currentLayout().applyCss(getStage());
		}
	}

	@FXML
	private void finishButtonHandler(ActionEvent evenet) {
		try {
			profile = Profile.load(profileComboBox.getSelectionModel().getSelectedItem());

			getStage().close();
		} catch (IOException | DocumentException | ProfileNotFoundException e) {
			showErrorMessage(Localization.getString(Strings.Error_Profile_Save, e.getLocalizedMessage()));
			e.printStackTrace();
		}
	}

	@FXML
	private void cancelButtonHandler(ActionEvent event) {
		getStage().close();
	}

	@FXML
	private void newProfileButtonHandler(ActionEvent event) {
		NewProfileDialog dialog = new NewProfileDialog(getStage());
		dialog.getStage().showAndWait();

		Profile profile = dialog.getProfile();

		// In GUI hinzufügen (am Ende) und auswählen
		if (profile != null) {
			profileComboBox.getItems().add(profile.getRef());
			profileComboBox.getSelectionModel().selectLast();
		}
	}

	public Profile getProfile() {
		return profile;
	}

}
