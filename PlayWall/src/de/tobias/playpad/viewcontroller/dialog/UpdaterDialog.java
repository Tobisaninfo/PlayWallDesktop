package de.tobias.playpad.viewcontroller.dialog;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.utils.util.Localization;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class UpdaterDialog extends Dialog<Void> {

	public UpdaterDialog(Window owner) {
		setHeaderText(Localization.getString(Strings.UI_Dialog_DragAndDrop_Header));
		setGraphic(new ImageView("org/controlsfx/dialog/dialog-information.png"));

		Label textLabel = new Label(
				"Die Aktualisierung wird vorbereitet. Bitte schließen Sie nicht das Programm. \nDieser Vorgang kann wenige Minuten dauern.");
		textLabel.setWrapText(true);
		textLabel.setMaxWidth(450);
		getDialogPane().setContent(textLabel);

		initOwner(owner);
		initModality(Modality.WINDOW_MODAL);
		Stage dialogStage = (Stage) getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(dialogStage.getIcons()::add);
	}
}