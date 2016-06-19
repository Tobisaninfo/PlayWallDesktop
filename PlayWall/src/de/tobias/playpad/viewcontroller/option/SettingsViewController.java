package de.tobias.playpad.viewcontroller.option;

import java.util.ArrayList;
import java.util.List;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.pad.conntent.PadContentRegistry;
import de.tobias.playpad.pad.conntent.UnkownPadContentException;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.viewcontroller.ISettingsViewController;
import de.tobias.playpad.viewcontroller.SettingsTabViewController;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

public class SettingsViewController extends ViewController implements ISettingsViewController {

	@FXML private TabPane tabPane;
	@FXML private ToggleButton lockedButton;
	@FXML private Button finishButton;

	protected List<SettingsTabViewController> tabs = new ArrayList<>();

	public SettingsViewController(Midi midiHandler, Screen screen, Window owner, Project project) {
		super("settingsView", "de/tobias/playpad/assets/view/option/", null, PlayPadMain.getUiResourceBundle());

		boolean activePlayer = project.getPlayedPlayers() > 0;

		addTab(new GeneralTabViewController(screen, this, activePlayer));
		addTab(new MappingTabViewController());
		addTab(new MidiTabViewController());
		addTab(new LayoutTabViewController());
		addTab(new PlayerTabViewController());

		// Content Types
		for (String type : PadContentRegistry.getTypes()) {
			try {
				SettingsTabViewController controller = PadContentRegistry.getPadContentConnect(type).getSettingsTabViewController(activePlayer);
				if (controller != null) {
					addTab(controller);
				}
			} catch (UnkownPadContentException e) {
				e.printStackTrace();
			}
		}

		// Listener
		PlayPadPlugin.getImplementation().getSettingsViewListener().forEach(l ->
		{
			try {
				l.onInit(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		addTab(new UpdateTabViewController());

		getStage().initOwner(owner);

		// Show Current Settings
		showCurrentSettings();
	}

	@Override
	public void init() {
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();

		// KeyCode
		addCloseKeyShortcut(() -> finishButton.fire());

		lockedButton.setGraphic(new FontIcon(FontAwesomeType.LOCK));
		lockedButton.setOnAction(e ->
		{
			boolean isLocked = lockedButton.isSelected();
			// Model
			profileSettings.setLocked(isLocked);

			// SettingsUI
			tabPane.setDisable(isLocked);
		});

		if (profileSettings.isLocked()) {
			// SettingsUI
			lockedButton.setSelected(true);
			tabPane.setDisable(true);
		}

		finishButton.defaultButtonProperty().bind(finishButton.focusedProperty());
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(715);
		stage.setMinHeight(700);
		stage.setTitle(Localization.getString(Strings.UI_Window_Settings_Title, Profile.currentProfile().getRef().getName()));

		Profile.currentProfile().currentLayout().applyCss(getStage());
	}

	// Copy Of Settings
	public boolean closeRequest() {
		boolean valid = true;
		for (SettingsTabViewController controller : tabs) {
			if (controller.validSettings() == false) {
				valid = false;
			}
		}

		if (valid) { // Einstellungen sind Valide
			// Listener
			PlayPadPlugin.getImplementation().getSettingsViewListener().forEach(l -> l.onClose(this));

			saveChanges();
			getStage().close();
			updateData();
			return true;
		} else {
			return false;
		}
	}

	// Settings aus AppSettings
	private void showCurrentSettings() {
		Profile profile = Profile.currentProfile();
		for (SettingsTabViewController controller : tabs) {
			controller.loadSettings(profile);
		}
	}

	private void saveChanges() {
		Profile profile = Profile.currentProfile();
		for (SettingsTabViewController controller : tabs) {
			controller.saveSettings(profile);
		}

		try {
			profile.save();
		} catch (Exception e) {
			showErrorMessage(Localization.getString(Strings.Error_Profile_Save, e.getLocalizedMessage()));
			e.printStackTrace();
		}
	}

	public boolean needUpdate() {
		boolean change = false;
		for (SettingsTabViewController controller : tabs) {
			if (controller.needReload()) {
				change = true;
			}
		}
		return change;
	}

	@FXML
	private void finishButtonHandler(ActionEvent event) {
		boolean valid = true;
		for (SettingsTabViewController controller : tabs) {
			if (controller.validSettings() == false) {
				valid = false;
			}
		}

		if (valid) { // Einstellungen sind Valide
			// Listener
			PlayPadPlugin.getImplementation().getSettingsViewListener().forEach(l -> l.onClose(this));

			saveChanges();
			getStage().close();
			updateData();
		}
	}

	public void addTab(SettingsTabViewController controller) {
		tabs.add(controller);
		tabPane.getTabs().add(new Tab(controller.name(), controller.getParent()));
	}
}
