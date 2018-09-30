package de.tobias.playpad.viewcontroller.option.profile;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.*;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.registry.Component;
import de.tobias.playpad.viewcontroller.BaseMapperListViewController;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;
import de.tobias.playpad.viewcontroller.cell.DisplayableCell;
import de.tobias.playpad.viewcontroller.cell.DisplayableTreeCell;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.IProfileReloadTask;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import de.tobias.utils.threading.Worker;
import de.tobias.utils.ui.NVC;
import de.tobias.utils.util.Localization;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MappingTabViewController extends ProfileSettingsTabViewController implements IMappingTabViewController, IProfileReloadTask {

	// Mapping Profiles
	@FXML
	private ComboBox<Mapping> mappingComboBox;

	@FXML
	private MenuButton editMappingsButton;
	@FXML
	private MenuItem mappingRenameButton;
	@FXML
	private MenuItem mappingExportButton;
	@FXML
	private MenuItem mappingDeleteButton;
	@FXML
	private MenuItem mappingDuplicateButton;
	@FXML
	private MenuItem mappingNewButton;
	@FXML
	private MenuItem mappingImportButton;

	// midi settings
	@FXML
	private CheckBox midiActiveCheckBox;
	@FXML
	private ComboBox<String> deviceComboBox;

	// Main View
	@FXML
	private TreeView<ActionDisplayable> treeView;

	@FXML
	private VBox detailView;
	private BaseMapperListViewController mapperListViewController;

	private Mapping oldMapping;
	private Mapping mapping;

	MappingTabViewController() {
		load("de/tobias/playpad/assets/view/option/profile/", "mapping", PlayPadMain.getUiResourceBundle());
	}

	@Override
	public void init() {
		mappingComboBox.setCellFactory(list -> new DisplayableCell<>());
		mappingComboBox.setButtonCell(new DisplayableCell<>());

		mappingComboBox.getSelectionModel().selectedItemProperty().addListener((a, b, c) ->
		{
			mapping = c;
			Profile.currentProfile().getMappings().setActiveMapping(c);
			createTreeViewContent();
		});

		// Midi
		MidiDevice.Info[] data = Midi.getMidiDevices();
		// Gerät anzeigen - Doppelte weg
		for (MidiDevice.Info item : data) {
			if (item != null) {
				if (!deviceComboBox.getItems().contains(item.getName())) {
					deviceComboBox.getItems().add(item.getName());

					// aktives Gerät wählen
					if (item.getName().equals(Profile.currentProfile().getProfileSettings().getMidiDevice())) {
						deviceComboBox.getSelectionModel().select(item.getName());
					}
				}
			}
		}
		midiActiveCheckBox.selectedProperty().addListener((a, b, c) -> deviceComboBox.setDisable(!c));

		// Main View
		treeView.getSelectionModel().selectedItemProperty().addListener((a, b, c) ->
		{
			detailView.getChildren().clear();

			if (c != null) {
				NVC controller = c.getValue().getSettingsViewController();
				if (controller == null) {
					controller = c.getValue().getActionSettingsViewController(mapping, this);
				}
				if (controller != null) {
					detailView.getChildren().add(controller.getParent());
				}
				if (c.getValue() instanceof Action) {
					showMapperFor((Action) c.getValue());
				}
			}
		});
		treeView.setCellFactory(list -> new DisplayableTreeCell<>());
	}

	private TreeItem<ActionDisplayable> createTreeView(Mapping mapping) {
		TreeItem<ActionDisplayable> rootItem = new TreeItem<>();
		Collection<ActionFactory> types = PlayPadPlugin.getRegistryCollection().getActions().getComponents();
		List<ActionFactory> sortedTypes = types.stream().sorted(Comparator.comparing(Component::getType)).collect(Collectors.toList());

		// Sort the tpyes for the treeview
		for (ActionType actionType : ActionType.values()) {
			createTreeViewForActionType(mapping, rootItem, sortedTypes, actionType);
		}

		return rootItem;
	}

	private void createTreeViewForActionType(Mapping mapping, TreeItem<ActionDisplayable> rootItem, List<ActionFactory> sortedTypes, ActionType type) {
		for (ActionFactory actionFactory : sortedTypes) {
			List<Action> actions = mapping.getActionsOfType(actionFactory);
			if (actionFactory.geActionType() == type) {
				TreeItem<ActionDisplayable> item = actionFactory.getTreeViewForActions(actions, mapping);
				rootItem.getChildren().add(item);
			}
		}
	}

	private void createTreeViewContent() {
		TreeItem<ActionDisplayable> rootItem = createTreeView(mapping);
		treeView.setRoot(rootItem);
	}

	private void setMappingItemsToList() {
		mappingComboBox.getItems().setAll(Profile.currentProfile().getMappings());
		mappingComboBox.setValue(Profile.currentProfile().getMappings().getActiveMapping());

		mapping = mappingComboBox.getValue();
	}

	@Override
	public void showMapperFor(Action action) {
		try {
			if (action != null) {
				mapperListViewController = BaseMapperListViewController.getInstance();
				mapperListViewController.showAction(action, detailView);
			} else {
				detailView.getChildren().remove(mapperListViewController.getParent());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isMidiActive() {
		return midiActiveCheckBox.isSelected();
	}

	// Event Handler
	@FXML
	private void deviceHandler(ActionEvent event) {
		ProfileSettings profilSettings = Profile.currentProfile().getProfileSettings();
		String device = deviceComboBox.getValue();

		// Ändern und Speichern
		if (device != null) {
			if (isMidiActive()) {
				Midi midi = Midi.getInstance();
				if (!device.equals(profilSettings.getMidiDevice()) || !midi.isOpen()) {
					try {
						// Setup
						midi.lookupMidiDevice(device);
						profilSettings.setMidiDeviceName(device);

						// UI Rückmeldung
						if (midi.getInputDevice() != null) {
							showInfoMessage(Localization.getString(Strings.Info_Midi_Device_Connected, device));
						}
					} catch (NullPointerException e) {
						showErrorMessage(Localization.getString(Strings.Error_Midi_Device_Unavailible, device));
						e.printStackTrace();
					} catch (IllegalArgumentException | MidiUnavailableException e) {
						showErrorMessage(Localization.getString(Strings.Error_Midi_Device_Busy, e.getLocalizedMessage()));
						e.printStackTrace();
					}
				}
			}
		}
	}

	@FXML
	private void mappingRenameHandler(ActionEvent event) {
		TextInputDialog dialog = new TextInputDialog(mapping.getName());
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(getContainingWindow());
		dialog.setHeaderText("Umbenennen");
		dialog.setContentText("Geben Sie einen neuen Namen für das Mapping Profil ein."); // TODO Localize
		dialog.showAndWait().filter(s -> !s.isEmpty()).ifPresent(mapping::setName);
	}


	@FXML
	private void mappingExportHandler(ActionEvent event) {
		if (mapping != null) {
			FileChooser chooser = new FileChooser();

			FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(Localization.getString(Strings.File_Filter_Preset), PlayPadMain.midiPresetType);
			chooser.getExtensionFilters().add(filter);

			File file = chooser.showSaveDialog(getContainingWindow());
			if (file != null) {
				Path path = file.toPath();

				Worker.runLater(() ->
				{
					try {
						MappingList.exportMidiPreset(path, mapping);
					} catch (IOException e) {
						e.printStackTrace();
						showErrorMessage(Localization.getString(Strings.Error_Preset_Export, e.getLocalizedMessage()));
					}
				});
			}
		}
	}

	@FXML
	private void mappingDeleteHandler(ActionEvent event) {
		final MappingList mappings = Profile.currentProfile().getMappings();
		int preset = mappingComboBox.getSelectionModel().getSelectedIndex();

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setContentText("Soll das Mapping Profile " + mappings.get(preset) + " wirklich gelöscht werden?");
		alert.initModality(Modality.WINDOW_MODAL);
		alert.initOwner(getContainingWindow());
		alert.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(result -> {
			mappings.remove(preset);
			mappingComboBox.getItems().remove(preset);

			if (mappings.size() == 1) {
				mappingDeleteButton.setDisable(true);
			}
		});
	}

	@FXML
	private void mappingDuplicateHandler(ActionEvent event) {
		try {
			Mapping clonedMapping = mapping.clone();
			clonedMapping.setName(Localization.getString(Strings.Standard_Copy, clonedMapping.getName()));

			// Model
			Profile.currentProfile().getMappings().add(clonedMapping);
			// UI
			mappingComboBox.getItems().add(clonedMapping);
			mappingComboBox.getSelectionModel().select(clonedMapping);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void mappingNewHandler(ActionEvent event) {
		Mapping preset = new Mapping(true);
		preset.initActionType(Profile.currentProfile());

		TextInputDialog dialog = new TextInputDialog(mapping.getName());
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(getContainingWindow());
		dialog.setHeaderText("Umbenennen");
		dialog.setContentText("Geben Sie einen Namen für das Mapping Profil ein."); // TODO Localize
		dialog.showAndWait().filter(s -> !s.isEmpty()).ifPresent(preset::setName);

		final MappingList mappings = Profile.currentProfile().getMappings();
		mappings.add(preset);
		mappingComboBox.getItems().add(preset);
		mappingComboBox.getSelectionModel().select(preset);

		if (mappings.size() > 1) {
			mappingDeleteButton.setDisable(false);
		}
	}

	@FXML
	private void mappingImportHandler(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(Localization.getString(Strings.File_Filter_Preset), PlayPadMain.midiPresetType));
		File file = chooser.showOpenDialog(getContainingWindow());
		if (file != null) {
			Path path = file.toPath();

			Worker.runLater(() ->
			{
				try {
					Mapping preset = MappingList.importMappingPreset(path, Profile.currentProfile());
					final MappingList mappingList = Profile.currentProfile().getMappings();
					mappingList.add(preset);
					Platform.runLater(() ->
					{
						mappingComboBox.getItems().add(preset);
						mappingComboBox.getSelectionModel().select(preset);

						if (mappingList.size() == 1) {
							mappingDeleteButton.setDisable(true);
						} else {
							mappingDeleteButton.setDisable(false);
						}

						// Rename preset if name already esists
						if (mappingList.containsName(preset.getName())) {
							TextInputDialog dialog = new TextInputDialog(mapping.getName());
							dialog.initModality(Modality.WINDOW_MODAL);
							dialog.initOwner(getContainingWindow());
							dialog.setHeaderText("Umbenennen");
							dialog.setContentText("Geben Sie einen neuen Namen für das Mapping Profil ein."); // TODO Localize
							dialog.showAndWait().filter(s -> !s.isEmpty()).ifPresent(mapping::setName);
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
					showErrorMessage(Localization.getString(Strings.Error_Preset_Import, e.getLocalizedMessage()));
				}
			});
		}

	}

	// Tab Utils
	@Override
	public void loadSettings(Profile profile) {
		oldMapping = profile.getMappings().getActiveMapping();
		setMappingItemsToList();
		createTreeViewContent();

		midiActiveCheckBox.setSelected(profile.getProfileSettings().isMidiActive());
		deviceComboBox.setDisable(!profile.getProfileSettings().isMidiActive());
		deviceComboBox.setValue(profile.getProfileSettings().getMidiDevice());
	}

	@Override
	public void saveSettings(Profile profile) {
		ProfileSettings profileSettings = profile.getProfileSettings();

		// Midi
		profileSettings.setMidiActive(isMidiActive());
	}

	@Override
	public boolean needReload() {
		return true;
	}

	@Override
	public Runnable getTask(ProfileSettings settings, Project project, IMainViewController controller) {
		return () -> {
			Profile.currentProfile().getMappings().getActiveMapping().adjustPadColorToMapper();

			Mapping activeMapping = Profile.currentProfile().getMappings().getActiveMapping();

			oldMapping.clearFeedback();
			activeMapping.showFeedback(project);
			activeMapping.initFeedbackType();
		};
	}

	@Override
	public boolean validSettings() {
		return true;
	}

	@Override
	public String name() {
		return Localization.getString(Strings.UI_Window_Settings_Mapping_Title);
	}
}
