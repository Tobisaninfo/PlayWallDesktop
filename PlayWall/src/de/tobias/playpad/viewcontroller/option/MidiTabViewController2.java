package de.tobias.playpad.viewcontroller.option;

import java.util.Collections;
import java.util.Optional;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.mididevice.Device;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.midi.MidiListener;
import de.tobias.playpad.model.Project;
import de.tobias.playpad.model.midi.Displayable;
import de.tobias.playpad.model.midi.MidiAction;
import de.tobias.playpad.model.midi.SubAction;
import de.tobias.playpad.model.midi.type.MidiKeyActionType;
import de.tobias.playpad.model.midi.type.MidiKeyActionTypes;
import de.tobias.playpad.model.midi.type.MidiKeyActionTypes.MidiKeyActionTypeStore;
import de.tobias.playpad.plugin.viewcontroller.IMainViewController;
import de.tobias.playpad.plugin.viewcontroller.IMidiTabViewController;
import de.tobias.playpad.plugin.viewcontroller.SettingsTabViewController;
import de.tobias.playpad.settings.MidiPreset;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.viewcontroller.PresetsViewController;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.ui.NotificationHandler;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.ui.scene.NotificationPane;
import de.tobias.utils.util.Localization;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MidiTabViewController2 extends SettingsTabViewController
		implements IMidiTabViewController, MidiListener, NotificationHandler, ChangeListener<TreeItem<Displayable>> {

	@FXML private AnchorPane rootPane;
	private NotificationPane notificationPane;

	@FXML private CheckBox midiActiveCheckBox;
	@FXML private ComboBox<String> deviceComboBox;

	@FXML private ComboBox<MidiPreset> presetsList;
	@FXML private Button presetsEditButton;
	@FXML private Button activateButton;

	@FXML private Button addMidiButton;
	@FXML private Button addMidiDraftButton;
	@FXML private Button draftButton;
	@FXML private Button deleteMidiButton;
	@FXML private Button clearMidiButton;

	@FXML private TreeView<Displayable> contentTreeView;
	@FXML private ComboBox<MidiKeyActionTypeStore> midiActionTypeComboBox;
	@FXML private AnchorPane settingsAnchorPane;

	// Midi Record
	private boolean recordMidi;
	private boolean useDraft;
	private org.controlsfx.control.action.Action midiRecordCancelAction;

	private final Window owner;

	public MidiTabViewController2(Window owner) {
		super("midiTab", "de/tobias/playpad/assets/view/option/", PlayPadMain.getUiResourceBundle());
		this.owner = owner;

		owner.setOnShown(event ->
		{
			if (!Midi.getInstance().isOpen() && Profile.currentProfile().getProfileSettings().isMidiActive()) {
				showError(Localization.getString(Strings.Info_Settings_Midi_NoDevice));
			}
			updateButtonDisable();
			if (!Profile.currentProfile().getMidiSetting().getDraftAction().isPresent()) {
				addMidiDraftButton.setDisable(true);
			}
		});

		// Midi Listener auf Einstellungen
		Midi.getInstance().setListener(this);
	}

	@Override
	public void init() {
		// Notifiation Pane
		midiRecordCancelAction = new org.controlsfx.control.action.Action(Localization.getString(Strings.Actions_Midi_Cancel), event ->
		{
			recordMidi = false;
			notificationPane.hide();
		});

		notificationPane = new NotificationPane(rootPane);
		notificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);

		presetsList.valueProperty().addListener((a, b, c) ->
		{
			if (c != null) {
				Collections.sort(c.getMidiActions()); // sortieren

				// Aktivieren Button ein und aus schalten
				if (c.isActive()) {
					activateButton.setDisable(true);
				} else {
					activateButton.setDisable(false);
				}

				// items in treeview
				showMidiPresetActions();
			}
		});

		// setup persets and select active or first
		presetsList.setItems(Profile.currentProfile().getMidiSetting().getPresets());
		presetsList.getSelectionModel().select(0); // Standart Selectend

		for (MidiPreset preset : Profile.currentProfile().getMidiSetting().getPresets()) { // Preset Auswählen
			if (preset.isActive()) {
				presetsList.getSelectionModel().select(preset); // Wenn aktiv dann selecten
				break;
			}
		}

		// Auswahlliste Links Setup
		showMidiPresetActions();
		contentTreeView.setShowRoot(false);
		contentTreeView.getSelectionModel().selectedItemProperty().addListener(this);

		// Action Types Init
		midiActionTypeComboBox.getItems().setAll(MidiKeyActionTypes.getActions());

		// ActionType Changed
		midiActionTypeComboBox.getSelectionModel().selectedItemProperty().addListener((a, b, c) ->
		{
			if (c != null) {
				try {
					MidiKeyActionType type = c.getActionType().newInstance();
					Optional<MidiAction> midiAction = getSelectedMidiAction();
					if (midiAction.isPresent()) {
						if (!MidiKeyActionType.equals(type, midiAction.get().getActionType())) {
							Device device = Midi.getInstance().getMidiDevice().get();

							midiAction.get().setActionType(type, device.getDefaultColor(type.getClass()));

							updateChildrenOfRootItem(midiAction.get(), getTreeObject(midiAction.get(), contentTreeView.getRoot()));
							showSettingsView(midiAction.get());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		deviceComboBox.setPlaceholder(new Label(Localization.getString(Strings.UI_Placeholder_MidiDevice)));

		Info[] data = Midi.getMidiDevices();
		// Gerät anzeigen - Doppelte weg
		for (Info item : data) {
			if (!deviceComboBox.getItems().contains(item.getName())) {
				deviceComboBox.getItems().add(item.getName());

				// aktives Gerät wählen
				if (item.getName().equals(Profile.currentProfile().getProfileSettings().getMidiDevice())) {
					deviceComboBox.getSelectionModel().select(item.getName());
				}
			}
		}
	}

	@Override
	public Parent getParent() {
		return notificationPane;
	}

	// UI Helper Getter und Setter
	private void showMidiPresetActions() {
		contentTreeView.setRoot(createMidiActionsTree(getSelectedPreset()));
	}

	public void updateChildrenOfRootItem(MidiAction midiAction) {
		updateChildrenOfRootItem(midiAction, getTreeObject(midiAction, contentTreeView.getRoot()));
	}

	public void updateChildrenOfRootItem(MidiAction midiAction, TreeItem<Displayable> root) {
		root.getChildren().clear();
		if (!midiAction.getActions().isEmpty() && midiAction.getActionType().showSubActions()) {
			for (SubAction action : midiAction.getActions()) {
				TreeItem<Displayable> treeItemAction = new TreeItem<>(action);
				root.getChildren().add(treeItemAction);
			}
		}
		root.setExpanded(true);
	}

	private void addMidiActionToTree(MidiAction midiAction) {
		TreeItem<Displayable> treeItemMidi = new TreeItem<>(midiAction);
		int index = getSelectedPreset().getMidiActions().indexOf(midiAction);

		contentTreeView.getRoot().getChildren().add(index, treeItemMidi);
		createMidiActionsTree(getSelectedPreset());
		selectMidiAction(midiAction);
	}

	private void updateButtonDisable() {
		Optional<Displayable> displayable = getSelectedAction();
		if (displayable.isPresent()) {
			if (displayable.get() instanceof MidiAction) { // Root Node -> MidiAction
				deleteMidiButton.setDisable(false);
				draftButton.setDisable(false);
			} else {
				deleteMidiButton.setDisable(true);
				draftButton.setDisable(true);
			}
			midiActionTypeComboBox.setDisable(false);
		} else {
			deleteMidiButton.setDisable(true);
			draftButton.setDisable(true);
			midiActionTypeComboBox.setDisable(true);
		}
	}

	private MidiPreset getSelectedPreset() {
		return presetsList.getSelectionModel().getSelectedItem();
	}

	public Optional<MidiAction> getSelectedMidiAction() {
		TreeItem<Displayable> item = contentTreeView.getSelectionModel().getSelectedItem();
		if (item != null) {
			if (item.getValue() instanceof MidiAction) {
				return Optional.of((MidiAction) item.getValue());
			} else {
				return Optional.of((MidiAction) item.getParent().getValue());
			}
		} else {
			return Optional.empty();
		}
	}

	public Optional<Displayable> getSelectedAction() {
		TreeItem<Displayable> item = contentTreeView.getSelectionModel().getSelectedItem();
		if (item != null) {
			return Optional.of(item.getValue());
		} else {
			return Optional.empty();
		}
	}

	public TreeItem<Displayable> getTreeObject(Displayable object, TreeItem<Displayable> root) {
		if (root.getValue() == object) {
			return root;
		} else {
			for (TreeItem<Displayable> child : root.getChildren()) {
				TreeItem<Displayable> result = getTreeObject(object, child);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}

	private void selectMidiKeyActionType(MidiKeyActionType type) {
		MidiKeyActionTypeStore store = MidiKeyActionTypes.getStoreForType(type);
		midiActionTypeComboBox.setValue(store);
	}

	// Show Methods
	private TreeItem<Displayable> createMidiActionsTree(MidiPreset preset) {
		TreeItem<Displayable> treeItemRoot = new TreeItem<>();

		if (preset != null) {
			for (MidiAction midiAction : preset.getMidiActions()) {
				TreeItem<Displayable> treeItemMidi = new TreeItem<>(midiAction);
				if (!midiAction.getActions().isEmpty() && midiAction.getActionType().showSubActions()) {
					for (SubAction action : midiAction.getActions()) {
						TreeItem<Displayable> treeItemAction = new TreeItem<>(action);
						treeItemMidi.getChildren().add(treeItemAction);
					}
				}
				treeItemRoot.getChildren().add(treeItemMidi);
			}
		}

		return treeItemRoot;
	}

	private void selectPressedMidiAction(MidiMessage message) {
		getSelectedPreset().getMidiActionForMidi(message).ifPresent(item -> Platform.runLater(() -> selectMidiAction(item)));
	}

	private void selectMidiAction(MidiAction midiAction) {
		// select item
		TreeItem<Displayable> treeItem = getTreeObject(midiAction, contentTreeView.getRoot());
		contentTreeView.getSelectionModel().select(treeItem);

		// scroll to index (only index)
		int index = contentTreeView.getSelectionModel().getSelectedIndex();
		contentTreeView.scrollTo(index);
	}

	/**
	 * 
	 * @param value
	 *            MidiAction or SubAction
	 */
	public void showSettingsView(Displayable value) {
		settingsAnchorPane.getChildren().clear();
		midiActionTypeComboBox.setValue(null);

		ContentViewController controller = null;
		if (value != null) {
			if (value instanceof MidiAction) {
				MidiAction midiAction = (MidiAction) value;
				MidiKeyActionType type = midiAction.getActionType();
				controller = type.getMainViewController(midiAction, this, this);
				selectMidiKeyActionType(midiAction.getActionType());

			} else if (value instanceof SubAction) {
				SubAction action = (SubAction) value;
				MidiAction midiAction = action.getMidiAction();

				MidiKeyActionType type = midiAction.getActionType();
				controller = type.getSubViewController(action, this, this);
				selectMidiKeyActionType(midiAction.getActionType());
			}
		}

		// TODO Overhead mit neuen VC fixen, Reuable machen
		if (controller != null) {
			ViewController.setAnchor(controller.getParent(), 14, 0, 14, 0);
			settingsAnchorPane.getChildren().add(controller.getParent());
		}
	}

	// Midi Device und Presets Choose
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
						if (midi.getInputDevice().isPresent()) {
							notify(Localization.getString(Strings.Info_Midi_Device_Connected, device),
									PlayPadMain.notificationDisplayTimeMillis);
							// mainPane.setDisable(false); BUG Disable / Enable der GUI wenn kein MIDI da ist
						}
					} catch (NullPointerException e) {
						showError(Localization.getString(Strings.Error_Midi_Device_Unavailible, device));
						e.printStackTrace();
					} catch (IllegalArgumentException | MidiUnavailableException e) {
						showError(Localization.getString(Strings.Error_Midi_Device_Busy, e.getLocalizedMessage()));
						e.printStackTrace();
					}
				}
			}
		}
	}

	@FXML
	private void presetsEditButtonHandler(ActionEvent event) {
		PresetsViewController controller = new PresetsViewController(owner);
		controller.getStage().showAndWait();
	}

	@FXML
	private void activateButtonHandler(ActionEvent event) {
		MidiPreset item = presetsList.getSelectionModel().getSelectedItem();
		item.setActive(true);
		activateButton.setDisable(true);
		PresetsViewController.disableInvalidPresets(true, item, Profile.currentProfile().getMidiSetting());
	}

	// MidiActionHandlers
	@FXML
	private void addMidiButtonHandler(ActionEvent event) {
		notificationPane.show(Localization.getString(Strings.Info_Midi_Record_Start), null, midiRecordCancelAction);
		recordMidi = true;
		useDraft = false;
	}

	@FXML
	private void addMidiDraftButtonHandler(ActionEvent event) {
		notificationPane.show(Localization.getString(Strings.Info_Midi_Record_Start), null, midiRecordCancelAction);
		recordMidi = true;
		useDraft = true;
	}

	@FXML
	private void draftButtonHandler(ActionEvent event) {
		Optional<MidiAction> midiAction = getSelectedMidiAction();
		if (midiAction.isPresent()) {
			try {
				Profile.currentProfile().getMidiSetting().setDraftAction(midiAction.get().clone());
				addMidiDraftButton.setDisable(false);
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
				showError(Localization.getString(Strings.Error_Standard_Gen, e.getLocalizedMessage()));
			}
		}
	}

	@FXML
	private void deleteMidiButtonHandler(ActionEvent event) {
		Optional<MidiAction> midiAction = getSelectedMidiAction();
		if (midiAction.isPresent()) {
			getSelectedPreset().removeAction(midiAction.get()); // Model

			// Remove from Tree UI
			TreeItem<Displayable> midiTreeItem = getTreeObject(midiAction.get(), contentTreeView.getRoot());
			contentTreeView.getRoot().getChildren().remove(midiTreeItem);
		}
	}

	@FXML
	private void clearMidiButtonHandler(ActionEvent event) {
		Alert alert = new Alert(AlertType.CONFIRMATION);

		alert.initOwner(owner);
		alert.initModality(Modality.WINDOW_MODAL);
		Stage dialog = (Stage) alert.getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(dialog.getIcons()::add);

		alert.setContentText(Localization.getString(Strings.Info_Settings_Midi_ClearPreset));
		alert.showAndWait().filter(item -> item == ButtonType.OK).ifPresent(item ->
		{
			getSelectedPreset().clearActions();
			showMidiPresetActions();
		});
	}

	// Accessor for User Input
	public boolean isMidiActive() {
		return midiActiveCheckBox.isSelected();
	}

	// Midi
	@Override
	public void onMidiAction(MidiMessage message) {
		if (message.getLength() >= 3) {
			if (message.getMessage()[2] != 0) {
				// Neuer Midi Key (RECORD)
				if (recordMidi) {
					recordMidi = false;
					notificationPane.hide();

					int midiCommand = message.getMessage()[0];
					byte midiKey = message.getMessage()[1];

					if (getSelectedPreset().isContaining(midiCommand, midiKey)) {
						notify(Localization.getString(Strings.Error_Midi_Record_Fail), PlayPadMain.notificationDisplayTimeMillis);
						return;
					}

					try {
						final MidiAction midiAction;

						// Neue Aktion
						if (useDraft == false) {
							midiAction = new MidiAction(midiCommand, midiKey, getSelectedPreset());
							if (midiAction != null) {
								Platform.runLater(() ->
								{
									// Model neue Midi Action
									getSelectedPreset().addAction(midiAction);
									Collections.sort(getSelectedPreset().getMidiActions());

									// GUI hinzufügen
									addMidiActionToTree(midiAction);
									selectPressedMidiAction(message);
								});
							}
						} else {
							// Verwende Vorlage und passe Midi an
							midiAction = Profile.currentProfile().getMidiSetting().getDraftAction().get().clone();
							midiAction.setMidiPreset(getSelectedPreset());
							midiAction.setMidiCommand(midiCommand);
							midiAction.setMidiKey(midiKey);

							// Cart ID ändern
							Platform.runLater(() ->
							{
								if (midiAction.getActionType().handleClone(midiAction, this)) {
									if (midiAction != null) {
										// Model neue Midi Action
										getSelectedPreset().addAction(midiAction);
										Collections.sort(getSelectedPreset().getMidiActions());

										// GUI hinzufügen
										addMidiActionToTree(midiAction);
										selectPressedMidiAction(message);
									}
								}
							});
						}
					} catch (Exception e) {
						showError(Localization.getString(Strings.Error_Standard_Gen, e.getLocalizedMessage()));
						e.printStackTrace();
					}
				} else {
					selectPressedMidiAction(message);
				}
			}
		}
	}

	// NotificationHandler
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
	public void show(String message, org.controlsfx.control.action.Action... action) {
		if (Platform.isFxApplicationThread()) {
			notificationPane.show(message, null, action);
		} else {
			Platform.runLater(() -> notificationPane.show(message, null, action));
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

	// Update Data (Wird in SettingsViewController bei showCurrentSettings aufgerufen)
	@Override
	public void updateData() {
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();
		deviceComboBox.setValue(profileSettings.getMidiDevice());
		midiActiveCheckBox.setSelected(profileSettings.isMidiActive());
	}

	// Display Settings ViewController der jeweiligen Action, basierend auf dem Type
	/*
	 * Wird aufgerufen, wenn in der Liste Links was ausgewählt wird
	 */
	@Override
	public void changed(ObservableValue<? extends TreeItem<Displayable>> observable, TreeItem<Displayable> oldValue,
			TreeItem<Displayable> newValue) {
		settingsAnchorPane.getChildren().clear();
		if (newValue != null)
			showSettingsView(newValue.getValue());
		updateButtonDisable();
	}

	@Override
	public void loadSettings(Profile profile) {
		updateData();
	}

	@Override
	public void saveSettings(Profile profile) {
		ProfileSettings profileSettings = profile.getProfileSettings();

		// Midi
		profileSettings.setMidiActive(isMidiActive());
	}

	@Override
	public boolean validSettings() {
		return true;
	}

	@Override
	public boolean needReload() {
		return true;
	}

	@Override
	public void reload(Profile profile, Project project, IMainViewController controller) {
		if (Midi.getInstance().getMidiDevice().isPresent())
			try {
				Midi.getInstance().getMidiDevice().get().showFeedbackForPage(controller.getPage(), controller.getPage(), project);
			} catch (MidiUnavailableException | InvalidMidiDataException e) {
				e.printStackTrace();
			}
	}

	@Override
	public String name() {
		return Localization.getString(Strings.UI_Window_Settings_Midi_Title);
	}
}
