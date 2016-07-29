package de.tobias.playpad.viewcontroller.option;

import java.util.ArrayList;
import java.util.List;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.registry.NoSuchComponentException;
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

	public SettingsViewController(Midi midiHandler, Screen currentScreen, Window owner, Project project) {
		super("settingsView", "de/tobias/playpad/assets/view/option/", null, PlayPadMain.getUiResourceBundle());

		boolean activePlayer = project.hasPlayedPlayers();

		addTab(new GeneralTabViewController(currentScreen, this, activePlayer));
		addTab(new MappingTabViewController());
		addTab(new MidiTabViewController());
		addTab(new DesignTabViewController());
		addTab(new PlayerTabViewController());

		// Custom Tabs - Content Types
		for (String type : PlayPadPlugin.getRegistryCollection().getPadContents().getTypes()) {
			try {
				PadContentConnect component = PlayPadPlugin.getRegistryCollection().getPadContents().getComponent(type);
				SettingsTabViewController controller = component.getSettingsTabViewController(activePlayer);
				if (controller != null) {
					addTab(controller);
				}
			} catch (NoSuchComponentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		addTab(new UpdateTabViewController());

		getStage().initOwner(owner);

		// Show Current Settings
		loadTabs();
	}

	@Override
	public void init() {
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();

		// KeyCode
		addCloseKeyShortcut(() -> finishButton.fire());

		// Look Button Listener
		lockedButton.setGraphic(new FontIcon(FontAwesomeType.LOCK));
		lockedButton.setOnAction(e ->
		{
			boolean isLocked = lockedButton.isSelected();
			profileSettings.setLocked(isLocked);
			setLookEnable(isLocked);
		});

		// Übernimmt die aktuellen Einstellungen des Look Button
		if (profileSettings.isLocked()) {
			lockedButton.setSelected(true);
			setLookEnable(true);
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

	/**
	 * Zeigt die aktuellen Einstellungen für die Tabs an.
	 */
	private void loadTabs() {
		Profile profile = Profile.currentProfile();
		for (SettingsTabViewController controller : tabs) {
			controller.loadSettings(profile);
		}
	}

	/**
	 * Speichert die Einstellungen der Tabs.
	 */
	private void saveTabs() {
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

	public boolean closeRequest() {
		return onFinish();
	}

	// Button Listener
	@FXML
	private void finishButtonHandler(ActionEvent event) {
		onFinish();
		getStage().close();
	}

	/**
	 * Speichert alle Informationen.
	 * 
	 * @return <code>true</code>Alle Einstellungen sind Valid.
	 */
	private boolean onFinish() {
		for (SettingsTabViewController controller : tabs) {
			if (controller.validSettings() == false) {
				return false;
			}
		}

		saveTabs();
		updateData(); // Reload MainViewController Settings // TODO Rewrite
		return true;
	}

	/**
	 * Aktiviert/Deaktiviert den Look Button.
	 * 
	 * @param isLocked
	 *            isLooked
	 */
	private void setLookEnable(boolean isLocked) {
		tabPane.setDisable(isLocked);
	}

	public void addTab(SettingsTabViewController controller) {
		tabs.add(controller);
		tabPane.getTabs().add(new Tab(controller.name(), controller.getParent()));
	}
}
