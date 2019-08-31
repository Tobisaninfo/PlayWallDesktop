package de.tobias.playpad.viewcontroller.dialog.profile;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
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
		dialog.getIcons().add(PlayPadPlugin.getInstance().getIcon());

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

		setContentText(Localization.getString(Strings.UI_DIALOG_NEW_PROFILE_CONTENT));
		showAndWait().filter(name -> !name.isEmpty()).ifPresent(name ->
		{
			try {
				newRef = new ProfileReference(name);
				ProfileReferenceManager.duplicate(cloneableProfile, newRef);
			} catch (IOException e) {
				Logger.error(e);
				controller.showErrorMessage(Localization.getString(Strings.ERROR_PROFILE_SAVE, e.getMessage()));
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
