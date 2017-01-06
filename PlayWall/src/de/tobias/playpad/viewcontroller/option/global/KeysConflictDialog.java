package de.tobias.playpad.viewcontroller.option.global;

import java.util.List;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.settings.keys.Key;
import de.tobias.playpad.settings.keys.KeyCollection;
import de.tobias.utils.util.Localization;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class KeysConflictDialog extends Alert {

	KeysConflictDialog(List<Key> conflicts, KeyCollection collection) {
		super(AlertType.ERROR);

		String keys = "";
		for (int i = 0; i < conflicts.size(); i++) {
			keys += "- " + collection.getName(conflicts.get(i).getId());
			if (i + 1 < conflicts.size()) {
				keys += "\n";
			}
		}
		setHeaderText(Localization.getString(Strings.UI_Settings_Keys_Conflict_Header));
		setContentText(Localization.getString(Strings.UI_Settings_Keys_Conflict_Content, keys));

		Stage dialogStage = (Stage) getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(dialogStage.getIcons()::add);
		initModality(Modality.WINDOW_MODAL);
	}
}
