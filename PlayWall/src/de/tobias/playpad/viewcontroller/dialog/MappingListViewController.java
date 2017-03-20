package de.tobias.playpad.viewcontroller.dialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.action.MappingList;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.viewcontroller.cell.DisplayableCell;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Worker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Dieser ViewController listet alle Mappings auf. Man kann diese importieren, exportieren und aktivieren/deaktivieren.
 * 
 * @author tobias
 *
 */
public class MappingListViewController extends ViewController {

	@FXML private ComboBox<Mapping> presetsListView;

	@FXML private TextField nameTextField;

	@FXML private Button addButton;
	@FXML private Button removeButton;
	@FXML private Button importButton;
	@FXML private Button exportButton;
	@FXML private Button duplicateButton;

	@FXML private Button finishButton;

	private MappingList mappingList;

	public MappingListViewController(MappingList mappingList, Window window) {
		super("mappingView", "de/tobias/playpad/assets/dialog/", null, PlayPadMain.getUiResourceBundle());
		this.mappingList = mappingList;

		getStage().initOwner(window);

		presetsListView.getItems().addAll(mappingList);
		presetsListView.getSelectionModel().selectFirst();

		if (mappingList.size() == 1) {
			removeButton.setDisable(true);
		}
	}

	@Override
	public void init() {
		presetsListView.setPlaceholder(new Label(Localization.getString(Strings.UI_Placeholder_Preset)));

		// Ausgewählte in der Liste rechts zeigen und Button disablen
		presetsListView.getSelectionModel().selectedItemProperty().addListener((a, b, c) ->
		{
			showPreset(c);
			if (c == null) {
				exportButton.setDisable(true);
				if (mappingList.size() <= 1)
					removeButton.setDisable(true);
			} else {
				removeButton.setDisable(false);
				exportButton.setDisable(false);
			}
		});

		// Name des Presets aktualisieren
		nameTextField.textProperty().addListener((a, b, c) ->
		{
			Mapping item = getSelectedMidiPreset();
			if (item != null) {
				item.setName(c);
			}
		});

		presetsListView.setCellFactory(item -> new DisplayableCell<>());
		presetsListView.setButtonCell(new DisplayableCell<>());

		addCloseKeyShortcut(() -> getStage().close());
	}

	@Override
	public void initStage(Stage stage) {
		stage.initModality(Modality.WINDOW_MODAL);
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(500);
		stage.setMinHeight(200);
		stage.setTitle(Localization.getString(Strings.UI_Dialog_Preset_Title));

		Profile.currentProfile().currentLayout().applyCss(getStage());
	}

	private void showPreset(Mapping midiPreset) {
		if (midiPreset != null) {
			nameTextField.setText(midiPreset.getName());
		} else {
			nameTextField.setText(null);
		}
	}

	@FXML
	private void addButtonHandler(ActionEvent event) {
		Mapping preset = new Mapping(true);

		mappingList.add(preset);
		presetsListView.getItems().add(preset);
		presetsListView.getSelectionModel().select(preset);

		if (mappingList.size() > 1) {
			removeButton.setDisable(false);
		}
	}

	@FXML
	private void duplicateButtonHandler() {
		Mapping preset = getSelectedMidiPreset();
		try {
			Mapping clonedMapping = preset.clone();
			clonedMapping.setName(Localization.getString(Strings.Standard_Copy, clonedMapping.getName()));

			// Model
			mappingList.add(clonedMapping);
			// UI
			presetsListView.getItems().add(preset);
			presetsListView.getSelectionModel().select(clonedMapping);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void removeButtonHandler(ActionEvent event) {
		int preset = presetsListView.getSelectionModel().getSelectedIndex();

		mappingList.remove(preset);
		presetsListView.getItems().remove(preset);

		if (mappingList.size() == 1) {
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
					Mapping preset = MappingList.importMappingPreset(path, Profile.currentProfile());
					mappingList.add(preset);
					Platform.runLater(() ->
					{
						presetsListView.getItems().add(preset);
						presetsListView.getSelectionModel().select(preset);

						if (mappingList.size() == 1) {
							removeButton.setDisable(true);
						} else {
							removeButton.setDisable(false);
						}

						showInfoMessage(Localization.getString(Strings.Standard_File_Save));
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
		Mapping preset = getSelectedMidiPreset();
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
						MappingList.exportMidiPreset(path, preset);
						showInfoMessage(Localization.getString(Strings.Standard_File_Save));
					} catch (IOException e) {
						e.printStackTrace();
						showErrorMessage(Localization.getString(Strings.Error_Preset_Export, e.getLocalizedMessage()));
					}
				});
			}
		}
	}

	private Mapping getSelectedMidiPreset() {
		return presetsListView.getSelectionModel().getSelectedItem();
	}

}
