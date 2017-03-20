package de.tobias.playpad.viewcontroller.option.profile;

import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiUnavailableException;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;

public class MidiTabViewController extends ProfileSettingsTabViewController {

	@FXML private AnchorPane rootPane;

	@FXML private CheckBox midiActiveCheckBox;
	@FXML private ComboBox<String> deviceComboBox;

	MidiTabViewController() {
		load("de/tobias/playpad/assets/view/option/profile/", "midiTab", PlayPadMain.getUiResourceBundle());

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
	public void init() {
		midiActiveCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			deviceComboBox.setDisable(!c);
		});
	}

	// Midi MidiDeviceImpl und Presets Choose
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

	public boolean isMidiActive() {
		return midiActiveCheckBox.isSelected();
	}

	@Override
	public void loadSettings(Profile profile) {
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
	public boolean validSettings() {
		return true;
	}

	@Override
	public boolean needReload() {
		return true;
	}

	@Override
	public String name() {
		return Localization.getString(Strings.UI_Window_Settings_Midi_Title);
	}
}
