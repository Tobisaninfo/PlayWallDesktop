package de.tobias.playpad.viewcontroller.dialog.profile;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.profile.ref.ProfileReferenceManager;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class ProfileDuplicateDialog extends TextInputDialog {

	private ProfileReference newRef;

	public ProfileDuplicateDialog(NVC controller, ProfileReference cloneableProfile) {
		super(cloneableProfile.getName());

		initOwner(controller.getContainingWindow());
		initModality(Modality.WINDOW_MODAL);
		Stage dialog = (Stage) getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(dialog.getIcons()::add);

		Button button = (Button) getDialogPane().lookupButton(ButtonType.OK);
		button.setDisable(true); // Initial disable
		getEditor().textProperty().addListener((a, b, c) ->
		{
			if (!ProfileReferenceManager.validateName(c)) {
				button.setDisable(true);
			} else {
				button.setDisable(false);
			}
		});

		setContentText(Localization.getString(Strings.UI_Dialog_NewProfile_Content));
		showAndWait().filter(name -> !name.isEmpty()).ifPresent(name ->
		{
			try {
				newRef = new ProfileReference(name);
				ProfileReferenceManager.duplicate(cloneableProfile, newRef);
			} catch (IOException e) {
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
