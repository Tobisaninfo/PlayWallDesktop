package de.tobias.playpad.viewcontroller.option.global;

import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.OS;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.settings.keys.Key;
import de.tobias.playpad.settings.keys.KeyCollection;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.GlobalSettingsTabViewController;
import de.tobias.playpad.viewcontroller.option.IGlobalReloadTask;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class KeysTabViewController extends GlobalSettingsTabViewController implements IGlobalReloadTask {

	@FXML
	private TextField searchTextField;

	@FXML
	private TableView<Key> table;
	@FXML
	private TableColumn<Key, String> shortcutTableColumn;
	@FXML
	private TableColumn<Key, String> nameTableColumn;

	@FXML
	private Label nameLabel;
	@FXML
	private Label shortcutLabel;
	@FXML
	private Button newShortcutButton;
	@FXML
	private Button deleteButton;

	private Key currentKey;
	private ObservableList<Key> keys = FXCollections.observableArrayList();

	KeysTabViewController() {
		load("view/option/global", "KeysTab", PlayPadMain.getUiResourceBundle());
	}

	@Override
	public void init() {
		shortcutTableColumn.setCellValueFactory(param -> param.getValue().displayProperty());
		nameTableColumn.setCellValueFactory(param ->
		{
			GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();
			return new SimpleStringProperty(globalSettings.getKeyCollection().getName(param.getValue().getId()));
		});

		table.getSelectionModel().selectedItemProperty().addListener((a, b, c) -> setDetailView(c));
		searchTextField.textProperty().addListener((a, b, c) -> search());

		table.setRowFactory(tv ->
		{
			TableRow<Key> row = new TableRow<>();
			row.setOnMouseClicked(event ->
			{
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					setDetailView(row.getItem());
					showNewKeyBindingDialog();
				}
			});
			return row;
		});

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
		showNewKeyBindingDialog();
	}

	@FXML
	void deleteHandler(ActionEvent event) {
		if (currentKey != null) {
			GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();
			globalSettings.getKeyCollection().removeKeyBinding(currentKey);
		}
	}

	private void showNewKeyBindingDialog() {
		Alert alert = new Alert(AlertType.NONE);
		alert.setContentText(Localization.getString(Strings.UI_Settings_Alert_NewKeyShortcut_Text));
		Scene scene = alert.getDialogPane().getScene();

		scene.setOnKeyPressed(ev ->
		{
			if (ev.getCode().isModifierKey()) {
				return;
			}

			boolean macCondition = ev.getCode().isLetterKey() || ev.getCode().isKeypadKey() || ev.getCode().isDigitKey()
					|| ev.getCode().isFunctionKey() || ev.getCode() == KeyCode.PERIOD || ev.getCode() == KeyCode.COMMA;

			if (OS.isWindows() || macCondition) {
				String key = ev.getCode().getName();

				Key newKey = new Key(currentKey.getId(), key, ev.isControlDown(), ev.isAltDown(), ev.isMetaDown(), ev.isShiftDown());

				GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();
				KeyCollection keyCollection = globalSettings.getKeyCollection();

				boolean conflict = keyCollection.keysConflict(newKey);
				if (!conflict) {
					keyCollection.editKey(newKey);

					shortcutLabel.setText(currentKey.toString());
					Platform.runLater(() -> ((Stage) scene.getWindow()).close());
				} else {
					KeysConflictDialog dialog = new KeysConflictDialog(keyCollection.getConflicts(newKey), keyCollection);
					dialog.initOwner(getContainingWindow());
					dialog.showAndWait();
				}
			}
		});

		alert.getButtonTypes().add(ButtonType.CANCEL);
		alert.initOwner(getContainingWindow());
		alert.initModality(Modality.WINDOW_MODAL);
		Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(alertStage.getIcons()::add);
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
	public Runnable getTask(GlobalSettings settings, IMainViewController controller) {
		return () -> controller.loadKeybinding(settings.getKeyCollection());
	}

	@Override
	public boolean validSettings() {
		return true;
	}

	@Override
	public String name() {
		return Localization.getString(Strings.UI_Window_Settings_Keys_Title);
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
