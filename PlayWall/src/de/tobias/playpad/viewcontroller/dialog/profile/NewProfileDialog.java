package de.tobias.playpad.viewcontroller.dialog.profile;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.design.modern.ModernGlobalDesign2;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;
import java.util.Optional;

public class NewProfileDialog extends NVC {

	@FXML private TextField nameTextField;
	@FXML private Button finishButton;
	@FXML private Button cancelButton;

	private Profile profile;

	public NewProfileDialog(Window owner) {
		load("de/tobias/playpad/assets/dialog/", "newProfileDialog", PlayPadMain.getUiResourceBundle());

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);
		nvcStage.getStage().sizeToScene();
		addCloseKeyShortcut(() -> getStageContainer().ifPresent(NVCStage::close));
	}

	public Optional<Profile> showAndWait() {
		getStageContainer().ifPresent(NVCStage::showAndWait);
		return Optional.ofNullable(profile);
	}

	@Override
	public void init() {
		nameTextField.textProperty().addListener((a, b, c) ->
		{
			if (c.isEmpty()) {
				finishButton.setDisable(true);
			} else {
				if (ProfileReferenceManager.getProfiles().containsProfileName(c)) {
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

		stage.setMinWidth(400);
		stage.setMinHeight(150);

		stage.setMaxWidth(500);

		stage.initModality(Modality.WINDOW_MODAL);

		if (Profile.currentProfile() != null) {
			ModernGlobalDesign2 design = Profile.currentProfile().getProfileSettings().getDesign();
			PlayPadPlugin.getModernDesignHandler().getModernGlobalDesignHandler().applyCss(design, stage);
		}
	}

	@FXML
	private void finishButtonHandler(ActionEvent event) {
		String name = nameTextField.getText();
		try {
			List<ProfileReference> profiles = ProfileReferenceManager.getProfiles();

			if (profiles.contains(name)) {
				showErrorMessage(Localization.getString(Strings.Error_Standard_NameInUse, name));
				return;
			}

			profile = ProfileReferenceManager.newProfile(name);
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
}
