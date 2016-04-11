package de.tobias.playpad.viewcontroller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.Strings;
import de.tobias.playpad.settings.MidiPreset;
import de.tobias.playpad.settings.MidiSettings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.cell.PresetCell;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.ui.NotificationHandler;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.ui.scene.NotificationPane;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Worker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class PresetsViewController extends ViewController implements NotificationHandler {

	@FXML private AnchorPane presetsContainer;
	@FXML private AnchorPane presetsAnchorPane;

	@FXML private ListView<MidiPreset> presetsListView;

	@FXML private TextField nameTextField;
	@FXML private CheckBox partlyCheckBox;
	@FXML private CheckBox activeCheckBox;
	@FXML private TextField pageTextField;

	@FXML private Button addButton;
	@FXML private Button removeButton;
	@FXML private Button importButton;
	@FXML private Button exportButton;
	@FXML private Button duplicateButton;
	@FXML private Button defaultButton;

	@FXML private Button finishButton;

	private NotificationPane notificationPane;
	private MidiSettings midiSettings;

	public PresetsViewController(Window window) {
		super("presetsView", "de/tobias/playpad/assets/dialog/", null, PlayPadMain.getUiResourceBundle());
		getStage().initOwner(window);

		presetsListView.setItems(midiSettings.getPresets());
		for (MidiPreset preset : midiSettings.getPresets()) {
			if (preset.isActive()) {
				presetsListView.getSelectionModel().select(preset);
				break;
			}
		}

		if (midiSettings.getPresets().size() == 1) {
			removeButton.setDisable(true);
		}
	}

	@Override
	public void init() {
		notificationPane = new NotificationPane(presetsAnchorPane);
		notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
		presetsContainer.getChildren().add(notificationPane);
		setAnchor(notificationPane, 0, 0, 0, 0);

		presetsListView.setPlaceholder(new Label(Localization.getString(Strings.UI_Placeholder_Preset)));

		// Ausgewählte in der Liste rechts zeigen und Button disablen
		presetsListView.getSelectionModel().selectedItemProperty().addListener((a, b, c) ->
		{
			showPreset(c);
			if (c == null) {
				exportButton.setDisable(true);
				if (midiSettings.getPresets().size() <= 1)
					removeButton.setDisable(true);
			} else {
				removeButton.setDisable(false);
				exportButton.setDisable(false);
			}
		});

		// Name des Presets aktualisieren
		nameTextField.textProperty().addListener((a, b, c) ->
		{
			MidiPreset item = getSelectedMidiPreset();
			if (item != null) {
				item.setName(c);
			}
		});

		activeCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			MidiPreset preset = getSelectedMidiPreset();
			if (c) {
				disableInvalidPresets(true, getSelectedMidiPreset(), midiSettings);
			}
			if (preset != null) {
				preset.setActive(c);
			}
		});

		partlyCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			MidiPreset item = getSelectedMidiPreset();
			if (item != null) {
				item.setPartly(c);
			}
		});

		pageTextField.textProperty().addListener((a, b, c) ->
		{
			MidiPreset preset = getSelectedMidiPreset();
			if (preset != null) {
				// Model Update
				if (c.isEmpty()) {
					preset.setPage(-1);
				} else {
					try {
						int page = Integer.valueOf(c) - 1;
						preset.setPage(page);
						pageTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, false);
					} catch (NumberFormatException e) {
						pageTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true);
					}
				}

				// Kollision zeigen
				if (preset.isActive()) {
					disableInvalidPresets(false, getSelectedMidiPreset(), midiSettings);
				}
			}
		});

		presetsListView.setCellFactory(item -> new PresetCell());

		getStage().getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN),
				() -> Platform.runLater(() -> getStage().close()));
	}

	public static List<MidiPreset> disableInvalidPresets(boolean disable, MidiPreset preset, MidiSettings midiSettings) {
		List<MidiPreset> disabledPresets = new ArrayList<>();

		// Neues Preset ist Global -> alle anderen weg
		if (preset.getPage() == -1) {
			for (MidiPreset item : midiSettings.getPresets()) {
				if (item != preset) {
					if (item.isActive()) {
						if (disable) {
							item.setActive(false);
						}
						disabledPresets.add(item);
					}
				}
			}
		} else {
			for (MidiPreset item : midiSettings.getPresets()) {
				if (item != preset) {
					if (item.isActive() && item.getPage() == -1 || item.isActive() && item.getPage() == preset.getPage()) {
						if (disable) {
							item.setActive(false);
						}
						disabledPresets.add(item);
					}
				}
			}
		}
		return disabledPresets;
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(600);
		stage.setMinHeight(400);
		stage.setTitle(Localization.getString(Strings.UI_Dialog_Preset_Title));

		Profile.currentProfile().currentLayout().applyCss(getStage());
	}

	private void showPreset(MidiPreset midiPreset) {
		if (midiPreset != null) {
			nameTextField.setText(midiPreset.getName());
			activeCheckBox.setSelected(midiPreset.isActive());
			partlyCheckBox.setSelected(midiPreset.isPartly());
			if (midiPreset.getPage() != -1) {
				pageTextField.setText(String.valueOf(midiPreset.getPage() + 1));
			} else {
				pageTextField.clear();
			}
			activeCheckBox.setDisable(false);
			partlyCheckBox.setDisable(false);
		} else {
			nameTextField.setText(null);
			activeCheckBox.setSelected(false);
			partlyCheckBox.setSelected(false);
			pageTextField.clear();
			activeCheckBox.setDisable(true);
			partlyCheckBox.setDisable(true);
		}
	}

	@FXML
	private void addButtonHandler(ActionEvent event) {
		MidiPreset preset = new MidiPreset();
		midiSettings.getPresets().add(preset);
		if (midiSettings.getPresets().size() > 1) {
			removeButton.setDisable(false);
		}
		presetsListView.getSelectionModel().select(preset);
	}

	@FXML
	private void removeButtonHandler(ActionEvent event) {
		midiSettings.getPresets().remove(presetsListView.getSelectionModel().getSelectedIndex());
		if (midiSettings.getPresets().size() == 1) {
			removeButton.setDisable(true);
		}
	}

	@FXML
	private void finishButtonHandler(ActionEvent event) {
		getStage().close();
	}

	@FXML
	private void importButtonHandler(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter(Localization.getString(Strings.File_Filter_Preset), PlayPadMain.midiPresetType));
		File file = chooser.showOpenDialog(getStage());
		if (file != null) {
			Path path = file.toPath();

			Worker.runLater(() ->
			{
				try {
					MidiPreset preset = midiSettings.importMidiPreset(path);
					preset.setActive(false); // DEFAULT nicht Active
					Platform.runLater(() ->
					{
						if (midiSettings.getPresets().size() == 1) {
							removeButton.setDisable(true);
						} else {
							removeButton.setDisable(false);
						}

						notify(Localization.getString(Strings.Standard_File_Save), PlayPadMain.notificationDisplayTimeMillis);
					});

				} catch (Exception e) {
					e.printStackTrace();
					showErrorMessage(Localization.getString(Strings.Error_Preset_Import, e.getLocalizedMessage()));
				}
			});
		}
	}

	@FXML
	private void exportButtonHandler(ActionEvent event) {
		MidiPreset preset = getSelectedMidiPreset();
		if (preset != null) {
			FileChooser chooser = new FileChooser();

			ExtensionFilter filter = new ExtensionFilter(Localization.getString(Strings.File_Filter_Preset), PlayPadMain.midiPresetType);
			chooser.getExtensionFilters().add(filter);

			File file = chooser.showSaveDialog(getStage());
			if (file != null) {
				Path path = file.toPath();

				Worker.runLater(() ->
				{
					try {
						midiSettings.exportMidiPreset(path, preset);
						notify(Localization.getString(Strings.Standard_File_Save), PlayPadMain.notificationDisplayTimeMillis);
					} catch (IOException e) {
						e.printStackTrace();
						showErrorMessage(Localization.getString(Strings.Error_Preset_Export, e.getLocalizedMessage()));
					}
				});
			}
		}
	}

	private MidiPreset getSelectedMidiPreset() {
		return presetsListView.getSelectionModel().getSelectedItem();
	}

	@FXML
	private void duplicateButtonHandler() {
		MidiPreset preset = getSelectedMidiPreset();
		try {
			MidiPreset preset2 = preset.clone();
			preset2.setName(Localization.getString(Strings.Standard_Copy, preset2.getName()));
			preset2.setActive(false);

//			Profile.currentProfile().getMidiSetting().getPresets().add(preset2);
			presetsListView.getSelectionModel().select(preset2);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void defaultButtonHandler() {
		Alert alert = new Alert(AlertType.CONFIRMATION);

		Stage dialog = (Stage) alert.getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(dialog.getIcons()::add);
		alert.initOwner(getStage());
		alert.initModality(Modality.WINDOW_MODAL);

		alert.setContentText(Localization.getString(Strings.Info_Settings_Preset_RestoreDefaults));
		alert.showAndWait().filter(button -> button == ButtonType.OK).ifPresent(button ->
		{
			try {
				Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, Profile.currentProfile().getName(), "Midi.xml");
				midiSettings.createDefaultSettings(path);
			} catch (Exception e) {
				e.printStackTrace();
				showErrorMessage(Localization.getString(Strings.Error_Preset_RestoreDefaults, e.getLocalizedMessage()));
			}
		});
	}

	// UI Info
	@Override
	public void notify(String text, long duration) {
		if (Platform.isFxApplicationThread()) {
			notificationPane.showAndHide(text, duration);
		} else {
			Platform.runLater(() -> notificationPane.showAndHide(text, duration));
		}
	}

	@Override
	public void notify(String text, long duration, Runnable finish) {
		if (Platform.isFxApplicationThread()) {
			notificationPane.showAndHide(text, duration, finish);
		} else {
			Platform.runLater(() -> notificationPane.showAndHide(text, duration, finish));
		}
	}

	@Override
	public void showError(String message) {
		if (Platform.isFxApplicationThread()) {
			notificationPane.showError(message);
		} else {
			Platform.runLater(() -> notificationPane.showError(message));
		}
	}

	@Override
	public void hide() {
		if (Platform.isFxApplicationThread()) {
			notificationPane.hide();
		} else {
			Platform.runLater(() -> notificationPane.hide());
		}
	}
}
