package de.tobias.playpad.viewcontroller.dialog;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.updater.client.Updatable;
import de.tobias.updater.client.UpdateRegistery;
import de.tobias.utils.nui.AdvancedDialog;
import de.tobias.utils.util.Localization;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Set;

public class AutoUpdateDialog extends AdvancedDialog {

	public AutoUpdateDialog(Window owner) {
		super(owner);
		Set<Updatable> updates = UpdateRegistery.getAvailableUpdates();

		StringBuilder builder = new StringBuilder();
		for (Updatable update : updates) {
			builder.append(update.name());
			builder.append(" ");
			builder.append(update.getNewVersion());
			builder.append("\n");
		}

		setTitle(Localization.getString(Strings.UI_Dialog_AutoUpdate_Title));
		setContent(Localization.getString(Strings.UI_Dialog_AutoUpdate_Content, builder.toString()));
		setHeaderText(Localization.getString(Strings.UI_Dialog_AutoUpdate_Header));
		setCheckboxText(Localization.getString(Strings.UI_Dialog_AutoUpdate_Checkbox));

		setIcon(PlayPadMain.stageIcon);

		ButtonType updateButton = new ButtonType(Localization.getString(Strings.UI_Dialog_AutoUpdate_Button_Update), ButtonData.APPLY);
		ButtonType cancelButton = new ButtonType(Localization.getString(Strings.UI_Dialog_AutoUpdate_Button_Cancel), ButtonData.CANCEL_CLOSE);

		addButtonType(updateButton);
		addButtonType(cancelButton);
	}
}