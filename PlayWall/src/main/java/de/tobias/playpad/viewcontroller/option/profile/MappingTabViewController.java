package de.tobias.playpad.viewcontroller.option.profile;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.MappingCollection;
import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.device.MidiDeviceInfo;
import de.thecodelabs.midi.midi.Midi;
import de.thecodelabs.midi.serialize.MappingSerializer;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.ActionProvider;
import de.tobias.playpad.action.ActionType;
import de.tobias.playpad.action.feedback.ColorAdjuster;
import de.tobias.playpad.action.settings.ActionSettingsEntry;
import de.tobias.playpad.action.settings.ActionSettingsMappable;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.registry.Component;
import de.tobias.playpad.viewcontroller.BaseMapperListViewController;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;
import de.tobias.playpad.viewcontroller.cell.DisplayableTreeCell;
import de.tobias.playpad.viewcontroller.cell.MappingListCell;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.IProfileReloadTask;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

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
	private TreeView<ActionSettingsEntry> treeView;

	@FXML
	private VBox detailView;
	private BaseMapperListViewController mapperListViewController;

	MappingTabViewController() {
		load("view/option/profile", "Mapping", Localization.getBundle());
	}

	@Override
	public void init() {
		mappingComboBox.setCellFactory(list -> new MappingListCell());
		mappingComboBox.setButtonCell(new MappingListCell());

		mappingComboBox.getSelectionModel().selectedItemProperty().addListener((a, b, c) ->
		{
			Profile.currentProfile().getMappings().setActiveMapping(c);
			createTreeViewContent();
		});

		// Midi
		MidiDeviceInfo[] data = Midi.getInstance().getMidiDevices();
		// Gerät anzeigen - Doppelte weg
		for (MidiDeviceInfo item : data) {
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
		treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
		{
			detailView.getChildren().clear();
			Mapping mapping = mappingComboBox.getSelectionModel().getSelectedItem();

			if (newValue != null) {
				NVC controller = newValue.getValue().getDetailSettingsController(mapping, this);

				if (controller != null) {
					detailView.getChildren().add(controller.getParent());
				}
				if (newValue.getValue() instanceof ActionSettingsMappable) {
					showMapperFor(((ActionSettingsMappable) newValue.getValue()).getAction());
				}
			}
		});

		treeView.setCellFactory(list -> new DisplayableTreeCell<>());
	}

	private TreeItem<ActionSettingsEntry> createTreeView(Mapping mapping) {
		TreeItem<ActionSettingsEntry> rootItem = new TreeItem<>();
		Collection<ActionProvider> types = PlayPadPlugin.getRegistries().getActions().getComponents();
		List<ActionProvider> sortedTypes = types.stream().sorted(Comparator.comparing(Component::getType)).collect(Collectors.toList());

		// Sort the tpyes for the treeview
		for (ActionType actionType : ActionType.values()) {
			createTreeViewForActionType(mapping, rootItem, sortedTypes, actionType);
		}

		return rootItem;
	}

	private void createTreeViewForActionType(Mapping mapping, TreeItem<ActionSettingsEntry> rootItem, List<ActionProvider> sortedTypes, ActionType type) {
		for (ActionProvider provider : sortedTypes) {
			List<Action> actions = mapping.getActionsForType(provider.getType());
			if (provider.getActionType() == type) {
				TreeItem<ActionSettingsEntry> item = provider.getTreeItemForActions(actions, mapping);
				rootItem.getChildren().add(item);
			}
		}
	}

	private void createTreeViewContent() {
		Mapping mapping = Mapping.getCurrentMapping();
		TreeItem<ActionSettingsEntry> rootItem = createTreeView(mapping);
		treeView.setRoot(rootItem);
	}

	private void setMappingItemsToList() {
		mappingComboBox.getItems().setAll(Profile.currentProfile().getMappings().getMappings());
		Profile.currentProfile().getMappings().getActiveMapping().ifPresent(m -> mappingComboBox.setValue(m));
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
			Logger.error(e);
		}
	}

	private boolean isMidiActive() {
		return midiActiveCheckBox.isSelected();
	}

	// Event Handler
	@FXML
	private void deviceHandler(ActionEvent event) {
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();
		String device = deviceComboBox.getValue();

		// Ändern und Speichern
		if (device != null) {
			if (isMidiActive()) {
				Midi midi = Midi.getInstance();
				if (!device.equals(profileSettings.getMidiDevice()) || !midi.isOpen()) {
					try {
						// Setup
						final MidiDeviceInfo midiDeviceInfo = midi.getMidiDeviceInfo(device);
						midi.openDevice(midiDeviceInfo);

						// UI Rückmeldung
						if (midi.isOpen()) {
							showInfoMessage(Localization.getString(Strings.Info_Midi_Device_Connected, device));
							profileSettings.setMidiDeviceName(device);
						}
					} catch (NullPointerException e) {
						showErrorMessage(Localization.getString(Strings.Error_Midi_Device_Unavailible, device));
						Logger.error(e);
					} catch (IllegalArgumentException | MidiUnavailableException e) {
						showErrorMessage(Localization.getString(Strings.Error_Midi_Device_Busy, e.getLocalizedMessage()));
						Logger.error(e);
					}
				}
			}
		}
	}

	@SuppressWarnings("Duplicates")
	@FXML
	private void mappingRenameHandler(ActionEvent event) {
		Mapping mapping = mappingComboBox.getSelectionModel().getSelectedItem();

		TextInputDialog dialog = new TextInputDialog(mapping.getName());
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(getContainingWindow());
		dialog.setHeaderText("Umbenennen");
		dialog.setContentText("Geben Sie einen neuen Namen für das Mapping Profil ein."); // TODO Localize
		dialog.showAndWait().filter(s -> !s.isEmpty()).ifPresent(mapping::setName);
	}


	@FXML
	private void mappingExportHandler(ActionEvent event) {
		Mapping mapping = mappingComboBox.getSelectionModel().getSelectedItem();
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
						MappingSerializer.save(mapping, path);
					} catch (IOException e) {
						Logger.error(e);
						showErrorMessage(Localization.getString(Strings.Error_Preset_Export, e.getLocalizedMessage()));
					}
				});
			}
		}
	}

	@FXML
	private void mappingDeleteHandler(ActionEvent event) {
		final MappingCollection mappings = Profile.currentProfile().getMappings();
		Mapping preset = mappingComboBox.getSelectionModel().getSelectedItem();

		if (mappings.getMappings().size() <= 1) {
			Alerts.getInstance()
					.createAlert(
							Alert.AlertType.INFORMATION,
							"Mapping",
							"Das Mapping kann nicht gelöscht werden, da kein anders existiert.",
							getContainingWindow()
					).show();
			return;
		}

		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		// TODO Localize
		alert.setContentText("Soll das Mapping Profile " + preset.getName() + " wirklich gelöscht werden?");
		alert.initModality(Modality.WINDOW_MODAL);
		alert.initOwner(getContainingWindow());
		alert.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(result -> {
			mappings.removeMapping(preset);
			mappingComboBox.getItems().remove(preset);

			if (mappings.count() == 1) {
				mappingDeleteButton.setDisable(true);
			}
		});
	}

	@FXML
	private void mappingDuplicateHandler(ActionEvent event) {
		Mapping mapping = mappingComboBox.getSelectionModel().getSelectedItem();

		Mapping clonedMapping = new Mapping(mapping);
		clonedMapping.setName(Localization.getString(Strings.Standard_Copy, clonedMapping.getName()));

		// Model
		Profile.currentProfile().getMappings().addMapping(clonedMapping);
		// UI
		mappingComboBox.getItems().add(clonedMapping);
		mappingComboBox.getSelectionModel().select(clonedMapping);
	}

	@FXML
	private void mappingNewHandler(ActionEvent event) {
		Mapping preset = Profile.createMappingWithDefaultActions();


		TextInputDialog dialog = new TextInputDialog();
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(getContainingWindow());
		dialog.setHeaderText("Umbenennen");
		dialog.setContentText("Geben Sie einen Namen für das Mapping Profil ein."); // TODO Localize
		dialog.showAndWait().filter(s -> !s.isEmpty()).ifPresent(preset::setName);

		final MappingCollection mappings = Profile.currentProfile().getMappings();
		mappings.addMapping(preset);
		mappingComboBox.getItems().add(preset);
		mappingComboBox.getSelectionModel().select(preset);

		if (mappings.count() > 1) {
			mappingDeleteButton.setDisable(false);
		}
	}

	@SuppressWarnings("Duplicates")
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
					Mapping preset = MappingSerializer.load(path);
					final MappingCollection mappingList = Profile.currentProfile().getMappings();
					mappingList.addMapping(preset);
					Platform.runLater(() ->
					{
						mappingComboBox.getItems().add(preset);
						mappingComboBox.getSelectionModel().select(preset);
						mappingDeleteButton.setDisable(mappingList.count() == 1);

						// Rename preset if name already esists
						if (mappingList.containsName(preset.getName())) {
							TextInputDialog dialog = new TextInputDialog(preset.getName());
							dialog.initModality(Modality.WINDOW_MODAL);
							dialog.initOwner(getContainingWindow());
							dialog.setHeaderText("Umbenennen");
							dialog.setContentText("Geben Sie einen neuen Namen für das Mapping Profil ein."); // TODO Localize
							dialog.showAndWait().filter(s -> !s.isEmpty()).ifPresent(preset::setName);
						}
					});

				} catch (Exception e) {
					Logger.error(e);
					showErrorMessage(Localization.getString(Strings.Error_Preset_Import, e.getLocalizedMessage()));
				}
			});
		}

	}

	// Tab Utils
	@Override
	public void loadSettings(Profile profile) {
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
			Midi.getInstance().clearFeedback();
			ColorAdjuster.applyColorsToKeys();
			Midi.getInstance().showFeedback();
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
