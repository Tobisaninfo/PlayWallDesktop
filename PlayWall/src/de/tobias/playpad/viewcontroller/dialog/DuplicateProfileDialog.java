package de.tobias.playpad.viewcontroller.dialog;

import java.util.Optional;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileReference;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DuplicateProfileDialog extends TextInputDialog {

	private ProfileReference newRef;

	public DuplicateProfileDialog(ViewController controller, ProfileReference cloneableProfile) {
		initOwner(controller.getStage());
		initModality(Modality.WINDOW_MODAL);
		Stage dialog = (Stage) getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(dialog.getIcons()::add);

		Button button = (Button) getDialogPane().lookupButton(ButtonType.OK);
		getEditor().textProperty().addListener((a, b, c) ->
		{
			if (ProfileReference.getProfiles().contains(c) || !c.matches(Profile.profileNameEx)) {
				button.setDisable(true);
			} else {
				button.setDisable(false);
			}
		});

		setContentText(Localization.getString(Strings.UI_Dialog_NewProfile_Content));
		showAndWait().filter(name -> !name.isEmpty()).ifPresent(name ->
		{
			try {
				if (ProfileReference.getProfiles().contains(name)) {
					controller.showErrorMessage(Localization.getString(Strings.Error_Standard_NameInUse, name));
					return;
				}

				newRef = new ProfileReference(name);
				ProfileReference.duplicate(cloneableProfile, newRef);

			} catch (Exception e) {
				e.printStackTrace();
				controller.showErrorMessage(Localization.getString(Strings.Error_Profile_Save, e.getMessage()));
			}
		});
	}

	public Optional<ProfileReference> getName() {
		if (newRef != null) {
			return Optional.of(newRef);
		} else {
			return Optional.empty();
		}
	}
}
