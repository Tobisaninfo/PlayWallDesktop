package de.tobias.playpad.viewcontroller.dialog;

import java.util.List;

import de.tobias.playpad.Strings;
import de.tobias.playpad.update.Updatable;
import de.tobias.playpad.update.UpdateRegistery;
import de.tobias.utils.util.Localization;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AutoUpdateDialog extends Dialog<ButtonType> {

	private CheckBox checkBox;

	public AutoUpdateDialog() {
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

		getDialogPane().setContent(vBox);
		getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
	}

	public boolean isIgnoreUpdate() {
		return checkBox.isSelected();
	}
}
