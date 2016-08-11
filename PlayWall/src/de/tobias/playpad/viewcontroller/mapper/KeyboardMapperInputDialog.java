package de.tobias.playpad.viewcontroller.mapper;

import java.util.Optional;

import de.tobias.playpad.action.mapper.KeyboardMapper;
import de.tobias.utils.util.StringUtils;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class KeyboardMapperInputDialog extends Alert {

	public KeyboardMapperInputDialog(KeyboardMapper mapper) {
		super(AlertType.NONE);

		Scene scene = getDialogPane().getScene();

		scene.setOnKeyPressed(ev ->
		{
			mapper.setKey(ev.getCode().getName());
			mapper.setCode(ev.getCode());
		});
		scene.setOnKeyReleased(ev ->
		{
			// Close on Finish (alert.close() does not work)
			((Stage) scene.getWindow()).close();
		});
		scene.setOnKeyTyped(ev ->
		{
			if (!StringUtils.isStringNotVisable(ev.getCharacter())) {
				mapper.setKey(ev.getCharacter().toUpperCase());
			}
		});
	}

	public boolean showInputDialog() {
		Optional<ButtonType> result = showAndWait();
		return !result.isPresent();
	}
}
