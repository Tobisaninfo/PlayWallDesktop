package de.tobias.playpad.viewcontroller.dialog.profile;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.Optional;

public class ProfileChooseDialog extends NVC {

	@FXML
	private ComboBox<ProfileReference> profileComboBox;
	@FXML
	private Button newProfileButton;

	@FXML
	private Button finishButton;
	@FXML
	private Button cancelButton;

	private Profile profile;

	public ProfileChooseDialog(Window owner) {
		load("view/dialog", "ProfileChooseDialog", PlayPadMain.getUiResourceBundle());

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);

		profileComboBox.getItems().addAll(ProfileReferenceManager.getProfiles());
		profileComboBox.getSelectionModel().selectFirst();
	}

	public Optional<Profile> showAndWait() {
		getStageContainer().ifPresent(NVCStage::showAndWait);
		return Optional.ofNullable(profile);
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setTitle(Localization.getString(Strings.UI_Dialog_ChooseProfile_Title));
		stage.setResizable(false);
		stage.setWidth(560);
		stage.setHeight(230);

		stage.initModality(Modality.WINDOW_MODAL);

		if (Profile.currentProfile() != null) {
			PlayPadPlugin.styleable().applyStyle(stage);
		}
	}

	@FXML
	private void finishButtonHandler(ActionEvent event) {
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
		dialog.showAndWait().ifPresent(profile -> {
			// Add new Profile to combo box and select it
			profileComboBox.getItems().add(profile.getRef());
			profileComboBox.getSelectionModel().selectLast();
		});
	}
}
