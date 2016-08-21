package de.tobias.playpad.viewcontroller.dialog;

import java.util.List;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.update.Updatable;
import de.tobias.playpad.update.UpdateRegistery;
import de.tobias.utils.util.Localization;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class AutoUpdateDialog extends Dialog<ButtonType> {

	private CheckBox checkBox;

	public AutoUpdateDialog(Window owner) {
		List<Updatable> updates = UpdateRegistery.getAvailableUpdates();

		StringBuilder builder = new StringBuilder();
		for (Updatable update : updates) {
			builder.append(update.name());
			builder.append(" ");
			builder.append(update.getNewVersion());
			builder.append("\n");
		}

		Label label = new Label(Localization.getString(Strings.UI_Dialog_AutoUpdate_Content, builder.toString()));
		checkBox = new CheckBox(Localization.getString(Strings.UI_Dialog_AutoUpdate_Checkbox));
		VBox vBox = new VBox(14, label, checkBox);

		setHeaderText(Localization.getString(Strings.UI_Dialog_AutoUpdate_Header));

		ButtonType updateButton = new ButtonType(Localization.getString(Strings.UI_Dialog_AutoUpdate_Butto_Update), ButtonData.APPLY);
		ButtonType cancelButton = new ButtonType(Localization.getString(Strings.UI_Dialog_AutoUpdate_Butto_Cancel), ButtonData.CANCEL_CLOSE);

		getDialogPane().setContent(vBox);
		getDialogPane().getButtonTypes().addAll(updateButton, cancelButton);

		initOwner(owner);
		initModality(Modality.WINDOW_MODAL);
		setTitle(Localization.getString(Strings.UI_Dialog_AutoUpdate_Title));
		Stage dialogStage = (Stage) getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(dialogStage.getIcons()::add);
	}

	public boolean isIgnoreUpdate() {
		return checkBox.isSelected();
	}
}
