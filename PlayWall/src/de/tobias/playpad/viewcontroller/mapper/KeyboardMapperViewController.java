package de.tobias.playpad.viewcontroller.mapper;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.mapper.KeyboardMapper;
import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.action.mapper.MapperViewController;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.StringUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class KeyboardMapperViewController extends MapperViewController {

	@FXML private Label keyLabel;
	@FXML private Button mappingButton;

	private KeyboardMapper mapper;

	public KeyboardMapperViewController() {
		super("keyboard", "de/tobias/playpad/assets/view/mapper/", PlayPadMain.getUiResourceBundle());
	}

	@Override
	public Mapper getMapper() {
		return mapper;
	}

	@Override
	public void hideFeedback() {}

	@Override
	public void showFeedback() {}

	private void setLabel() {
		keyLabel.setText(mapper.getReadableName());
	}

	@FXML
	private void mappingButtonHandler(ActionEvent event) {
		Alert alert = new Alert(AlertType.NONE);
		alert.setTitle(Localization.getString(Strings.Mapper_Keyboard_Name));
		alert.setContentText(Localization.getString(Strings.Info_Mapper_PressKey));
		Scene scene = alert.getDialogPane().getScene();

		scene.setOnKeyPressed(ev ->
		{
			mapper.setKey(ev.getCode().getName());
			mapper.setCode(ev.getCode());
			setLabel();
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
				setLabel();
			}
		});

		alert.getButtonTypes().add(ButtonType.CANCEL);
		alert.initOwner(getWindow());
		alert.showAndWait();
	}

	@Override
	public void showInputMapperUI() {
		mappingButton.fire();
	}

	public void setMapper(KeyboardMapper keyboardMapper) {
		this.mapper = keyboardMapper;
		setLabel();
	}
}
