package de.tobias.playpad.viewcontroller.option.global;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.settings.keys.Key;
import de.tobias.playpad.settings.keys.KeyCollection;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class KeysConflictDialog extends Alert {

	KeysConflictDialog(List<Key> conflicts, KeyCollection collection) {
		super(AlertType.ERROR);

		StringBuilder keys = new StringBuilder();
		for (int i = 0; i < conflicts.size(); i++) {
			keys.append("- ").append(collection.getName(conflicts.get(i).getId()));
			if (i + 1 < conflicts.size()) {
				keys.append("\n");
			}
		}
		setHeaderText(Localization.getString(Strings.UI_SETTINGS_KEYS_CONFLICT_HEADER));
		setContentText(Localization.getString(Strings.UI_SETTINGS_KEYS_CONFLICT_CONTENT, keys.toString()));

		Stage dialogStage = (Stage) getDialogPane().getScene().getWindow();
		dialogStage.getIcons().add(PlayPadPlugin.getInstance().getIcon());
		initModality(Modality.WINDOW_MODAL);
	}
}
