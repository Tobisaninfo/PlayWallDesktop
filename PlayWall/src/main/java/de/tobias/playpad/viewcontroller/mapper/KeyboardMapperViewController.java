package de.tobias.playpad.viewcontroller.mapper;

import de.thecodelabs.midi.mapping.Key;
import de.thecodelabs.midi.mapping.KeyboardKey;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.StringUtils;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.mapper.MapperViewController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

public class KeyboardMapperViewController extends MapperViewController {

	@FXML
	private Label keyLabel;
	@FXML
	private Button mappingButton;

	private KeyboardKey mapper;

	public KeyboardMapperViewController() {
		load("view/mapper", "Keyboard", Localization.getBundle());
	}

	@Override
	public Key getKey() {
		return mapper;
	}

	@Override
	public void hideFeedback() {
	}

	@Override
	public void showFeedback() {
	}

	private String getReadableName() {
		if (mapper.getKey() == null || !StringUtils.isStringNotVisable(mapper.getKey())) {
			return mapper.getKey();
		} else {
			return mapper.getCode().getName();
		}
	}

	private void setLabel() {
		keyLabel.setText(getReadableName());
	}

	@FXML
	private void mappingButtonHandler(ActionEvent event) {
		inputDialog();
	}

	private boolean inputDialog() {
		KeyboardMapperInputDialog alert = new KeyboardMapperInputDialog(mapper);
		alert.setTitle(Localization.getString(Strings.MAPPER_KEYBOARD_NAME));
		alert.setContentText(Localization.getString(Strings.INFO_MAPPER_PRESS_KEY));

		alert.getButtonTypes().add(ButtonType.CANCEL);
		alert.initOwner(getContainingWindow());
		boolean result = alert.showInputDialog();
		setLabel();
		return result;
	}

	@Override
	public boolean showInputMapperUI() {
		return inputDialog();
	}

	@Override
	public void setKey(Key keyboardMapper) {
		this.mapper = (KeyboardKey) keyboardMapper;
		setLabel();
	}
}
