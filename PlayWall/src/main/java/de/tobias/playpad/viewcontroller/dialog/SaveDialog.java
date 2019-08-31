package de.tobias.playpad.viewcontroller.dialog;

import de.thecodelabs.utils.ui.AdvancedDialog;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

public class SaveDialog extends AdvancedDialog {

	public SaveDialog(Window owner) {
		super(owner);

		setTitle(Localization.getString(Strings.UI_DIALOG_SAVE_TITLE));
		setContent(Localization.getString(Strings.UI_DIALOG_SAVE_CONTENT));
		setHeaderText(Localization.getString(Strings.UI_DIALOG_SAVE_HEADER));
		setCheckboxText(Localization.getString(Strings.UI_DIALOG_SAVE_CHECKBOX));
		setIcon(PlayPadPlugin.getInstance().getIcon());

		ButtonType saveButton = new ButtonType(Localization.getString(Strings.UI_DIALOG_SAVE_BUTTON_YES), ButtonData.YES);
		ButtonType notSaveButton = new ButtonType(Localization.getString(Strings.UI_DIALOG_SAVE_BUTTON_NO), ButtonData.NO);
		ButtonType cancelButton = new ButtonType(Localization.getString(Strings.UI_DIALOG_SAVE_BUTTON_CANCEL), ButtonData.CANCEL_CLOSE);

		addButtonType(saveButton);
		addButtonType(notSaveButton);
		addButtonType(cancelButton);
	}

}
