package de.tobias.playpad.viewcontroller.dialog;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.utils.util.Localization;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class DragAndDropDialog extends Dialog<Void> {

	private CheckBox displayOnceCheckBox;

	public DragAndDropDialog(Window owner) {
		setHeaderText(Localization.getString(Strings.UI_Dialog_DragAndDrop_Header));
		setGraphic(new ImageView("org/controlsfx/dialog/dialog-information.png"));

		Label textLabel = new Label(Localization.getString(Strings.UI_Dialog_DragAndDrop_Content));
		textLabel.setWrapText(true);
		textLabel.setMaxWidth(450);
		ImageView view = new ImageView("de/tobias/playpad/assets/files/dialogDnD.png");

		displayOnceCheckBox = new CheckBox(Localization.getString(Strings.UI_Standard_DoNotShow));

		VBox box = new VBox(textLabel, view, displayOnceCheckBox);
		box.setSpacing(10);
		getDialogPane().setContent(box);

		ButtonType buttonTypeOk = new ButtonType(Localization.getString(Strings.UI_Dialog_DragAndDrop_Button), ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().add(buttonTypeOk);

		initOwner(owner);
		initModality(Modality.WINDOW_MODAL);
		Stage dialogStage = (Stage) getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(dialogStage.getIcons()::add);
	}

	public boolean isPermanentSelected() {
		return displayOnceCheckBox.isSelected();
	}
}
