package de.tobias.playpad.viewcontroller.dialog;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferences;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.dom4j.DocumentException;

import java.io.IOException;

public class ProfileChooseDialog extends NVC {

	@FXML private ComboBox<ProfileReference> profileComboBox;
	@FXML private Button newProfileButton;

	@FXML private Button finishButton;
	@FXML private Button cancelButton;

	private Profile profile;

	public ProfileChooseDialog(Window owner) {
		load("de/tobias/playpad/assets/dialog/", "profileChooseDialog", PlayPadMain.getUiResourceBundle());

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);

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

		stage.initModality(Modality.WINDOW_MODAL);

		if (Profile.currentProfile() != null) {
			Profile.currentProfile().currentLayout().applyCss(stage);
		}
	}

	@FXML
	private void finishButtonHandler(ActionEvent evenet) {
		try {
			profile = Profile.load(profileComboBox.getSelectionModel().getSelectedItem());

			getStageContainer().ifPresent(NVCStage::close);
		} catch (IOException | DocumentException | ProfileNotFoundException e) {
			showErrorMessage(Localization.getString(Strings.Error_Profile_Save, e.getLocalizedMessage()));
			e.printStackTrace();
		}
	}

	@FXML
	private void cancelButtonHandler(ActionEvent event) {
		getStageContainer().ifPresent(NVCStage::close);
	}

	@FXML
	private void newProfileButtonHandler(ActionEvent event) {
		NewProfileDialog dialog = new NewProfileDialog(getContainingWindow());
		dialog.getStageContainer().ifPresent(NVCStage::showAndWait);

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
