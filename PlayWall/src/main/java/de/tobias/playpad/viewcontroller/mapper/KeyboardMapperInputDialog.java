package de.tobias.playpad.viewcontroller.mapper;

import de.thecodelabs.midi.mapping.KeyboardKey;
import de.thecodelabs.utils.util.StringUtils;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.Optional;

public class KeyboardMapperInputDialog extends Alert {

	public KeyboardMapperInputDialog(KeyboardKey mapper) {
		super(AlertType.NONE);

		Scene scene = getDialogPane().getScene();

		scene.setOnKeyPressed(ev ->
		{
			if (checkControlKeys(ev)) return;
			mapper.setKey(ev.getCode().getName());
			mapper.setCode(ev.getCode());
		});
		scene.setOnKeyReleased(ev ->
		{
			if (checkControlKeys(ev)) return;
			// Close on Finish (alert.close() does not work)
			((Stage) scene.getWindow()).close();
		});
		scene.setOnKeyTyped(ev ->
		{
			if (checkControlKeys(ev)) return;
			if (!StringUtils.isStringNotVisable(ev.getCharacter())) {
				mapper.setKey(ev.getCharacter().toUpperCase());
			}
		});
	}

	private boolean checkControlKeys(KeyEvent ev) {
		return ev.isShortcutDown() || ev.isAltDown() || ev.isShiftDown();
	}

	public boolean showInputDialog() {
		Optional<ButtonType> result = showAndWait();
		return !result.isPresent();
	}
}
