package de.tobias.playpad.viewcontroller.dialog.profile;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
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

	@FXML
	private TextField nameTextField;
	@FXML
	private Button finishButton;
	@FXML
	private Button cancelButton;

	private Profile profile;

	public NewProfileDialog(Window owner) {
		load("view/dialog", "NewProfileDialog", Localization.getBundle());

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
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());
		PlayPadPlugin.styleable().applyStyle(stage);
		stage.initModality(Modality.WINDOW_MODAL);

		stage.setTitle(Localization.getString(Strings.UI_DIALOG_NEW_PROFILE_TITLE));
		stage.setWidth(400);
		stage.setHeight(200);

		stage.setMinWidth(400);
		stage.setMinHeight(150);

		stage.setMaxWidth(500);

	}

	@FXML
	private void finishButtonHandler(ActionEvent event) {
		String name = nameTextField.getText();
		try {
			List<ProfileReference> profiles = ProfileReferenceManager.getProfiles();

			if (profiles.contains(name)) {
				showErrorMessage(Localization.getString(Strings.ERROR_STANDARD_NAME_IN_USE, name));
				return;
			}

			profile = ProfileReferenceManager.newProfile(name);
			profile.save();
			getStageContainer().ifPresent(NVCStage::close);
		} catch (Exception e) {
			Logger.error(e);
			showErrorMessage(Localization.getString(Strings.ERROR_PROFILE_CREATE, e.getMessage()));
		}
	}

	@FXML
	private void cancelButtonHandler(ActionEvent event) {
		getStageContainer().ifPresent(NVCStage::close);
	}
}
