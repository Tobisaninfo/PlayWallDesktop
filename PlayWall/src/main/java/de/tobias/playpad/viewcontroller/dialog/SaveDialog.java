package de.tobias.playpad.viewcontroller.dialog;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.utils.ui.AdvancedDialog;
import de.tobias.utils.util.Localization;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

public class SaveDialog extends AdvancedDialog {

	public SaveDialog(Window owner) {
		super(owner);

		setTitle(Localization.getString(Strings.UI_Dialog_Save_Title));
		setContent(Localization.getString(Strings.UI_Dialog_Save_Content));
		setHeaderText(Localization.getString(Strings.UI_Dialog_Save_Header));
		setCheckboxText(Localization.getString(Strings.UI_Dialog_Save_Checkbox));
		setIcon(PlayPadMain.stageIcon);

		ButtonType saveButton = new ButtonType(Localization.getString(Strings.UI_Dialog_Save_Button_Yes), ButtonData.YES);
		ButtonType notSaveButton = new ButtonType(Localization.getString(Strings.UI_Dialog_Save_Button_No), ButtonData.NO);
		ButtonType cancelButton = new ButtonType(Localization.getString(Strings.UI_Dialog_Save_Button_Cancel), ButtonData.CANCEL_CLOSE);

		addButtonType(saveButton);
		addButtonType(notSaveButton);
		addButtonType(cancelButton);
	}

}
