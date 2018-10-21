package de.tobias.playpad.viewcontroller.mapper;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.mapper.KeyboardMapper;
import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.action.mapper.MapperViewController;
import de.tobias.utils.util.Localization;
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

	private KeyboardMapper mapper;

	public KeyboardMapperViewController() {
		load("view/mapper", "Keyboard", PlayPadMain.getUiResourceBundle());
	}

	@Override
	public Mapper getMapper() {
		return mapper;
	}

	@Override
	public void hideFeedback() {
	}

	@Override
	public void showFeedback() {
	}

	private void setLabel() {
		keyLabel.setText(mapper.getReadableName());
	}

	@FXML
	private void mappingButtonHandler(ActionEvent event) {
		inputDialog();
	}

	private boolean inputDialog() {
		KeyboardMapperInputDialog alert = new KeyboardMapperInputDialog(mapper);
		alert.setTitle(Localization.getString(Strings.Mapper_Keyboard_Name));
		alert.setContentText(Localization.getString(Strings.Info_Mapper_PressKey));

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

	public void setMapper(KeyboardMapper keyboardMapper) {
		this.mapper = keyboardMapper;
		setLabel();
	}
}
