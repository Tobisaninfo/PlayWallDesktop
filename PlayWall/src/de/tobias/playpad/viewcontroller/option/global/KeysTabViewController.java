package de.tobias.playpad.viewcontroller.option.global;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.settings.keys.Key;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.GlobalSettingsTabViewController;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.OS;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class KeysTabViewController extends GlobalSettingsTabViewController {

	@FXML private TextField searchTextField;

	@FXML private TableView<Key> table;
	@FXML private TableColumn<Key, String> shortcutTableColumn;
	@FXML private TableColumn<Key, String> nameTableColumn;

	@FXML private Label nameLabel;
	@FXML private Label shortcutLabel;
	@FXML private Button newShortcutButton;

	private Key currentKey;
	private ObservableList<Key> keys = FXCollections.observableArrayList();

	public KeysTabViewController() {
		super("keysTab", "de/tobias/playpad/assets/view/option/global/", PlayPadMain.getUiResourceBundle());
	}

	@Override
	public void init() {
		shortcutTableColumn.setCellValueFactory(param ->
		{
			return param.getValue().displayProperty();
		});
		nameTableColumn.setCellValueFactory(param ->
		{
			GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();
			return new SimpleStringProperty(globalSettings.getKeyCollection().getName(param.getValue().getId()));
		});

		table.getSelectionModel().selectedItemProperty().addListener((a, b, c) ->
		{
			setDetailView(c);
		});
		searchTextField.textProperty().addListener((a, b, c) -> search());
	}

	private void setDetailView(Key key) {
		currentKey = key;

		if (key != null) {
			GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();

			String name = globalSettings.getKeyCollection().getName(key.getId());
			nameLabel.setText(name);

			shortcutLabel.setText(key.toString());
			newShortcutButton.setDisable(false);
		} else {
			nameLabel.setText("");
			shortcutLabel.setText("");
			newShortcutButton.setDisable(true);
		}
	}

	@FXML
	void newShortcutButtonHandler(ActionEvent event) {
		Alert alert = new Alert(AlertType.NONE);
		alert.setContentText(Localization.getString(Strings.UI_Settings_Alert_NewKeyShortcut_Text));
		Scene scene = alert.getDialogPane().getScene();

		scene.setOnKeyPressed(ev ->
		{
			boolean macCondition = ev.getCode().isLetterKey() || ev.getCode().isKeypadKey() || ev.getCode().isDigitKey()
					|| ev.getCode().isFunctionKey() || ev.getCode() == KeyCode.PERIOD || ev.getCode() == KeyCode.COMMA;

			if (OS.isWindows() || macCondition) {
				String key = ev.getCode().getName();

				Key newKey = new Key(currentKey.getId(), key, ev.isControlDown(), ev.isAltDown(), ev.isMetaDown(), ev.isShiftDown());

				GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();
				boolean conflict = globalSettings.getKeyCollection().keysConflict(newKey);
				if (!conflict) {
					globalSettings.getKeyCollection().editKey(newKey);

					shortcutLabel.setText(currentKey.toString());
					Platform.runLater(() -> ((Stage) scene.getWindow()).close());
				} else {
					showErrorMessage("Konflikt"); // TODO Localize
				}
			}
		});

		alert.getButtonTypes().add(ButtonType.CANCEL);
		alert.initOwner(getWindow());
		alert.showAndWait();
	}

	@Override
	public void loadSettings(GlobalSettings settings) {
		keys.setAll(settings.getKeyCollection().getKeys());
		table.setItems(keys);
	}

	@Override
	public void saveSettings(GlobalSettings settings) {
		// Passiert beim Drücken von Tasten automatisch
	}

	@Override
	public boolean needReload() {
		return true;
	}

	@Override
	public void reload(GlobalSettings settings, Project project, IMainViewController controller) {
		controller.loadKeybinding(settings.getKeyCollection());
	}

	@Override
	public boolean validSettings() {
		return true;
	}

	@Override
	public String name() {
		// TODO Auto-generated method stub
		return "Keyboard (I18N)";
	}

	private void search() {
		FilteredList<Key> filteredData = new FilteredList<>(keys, s -> true);
		String search = searchTextField.getText();
		if (search == null || search.length() == 0) {
			filteredData.setPredicate(s -> true);
		} else {
			GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();
			filteredData.setPredicate(s -> globalSettings.getKeyCollection().getName(s.getId()).toLowerCase().startsWith(search.toLowerCase()));
		}
		table.setItems(filteredData);
	}
}
