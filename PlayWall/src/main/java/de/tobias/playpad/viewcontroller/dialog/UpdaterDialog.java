package de.tobias.playpad.viewcontroller.dialog;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class UpdaterDialog extends Dialog<Void> {

	public UpdaterDialog(Window owner) {
		setGraphic(new ImageView("org/controlsfx/dialog/dialog-information.png"));

		Label textLabel = new Label(Localization.getString(Strings.UI_DIALOG_UPDATE_INFO));
		textLabel.setWrapText(true);
		textLabel.setMaxWidth(450);
		getDialogPane().setContent(textLabel);

		initOwner(owner);
		initModality(Modality.WINDOW_MODAL);
		Stage dialogStage = (Stage) getDialogPane().getScene().getWindow();
		dialogStage.getIcons().add(PlayPadPlugin.getInstance().getIcon());
	}
}