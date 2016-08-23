package de.tobias.playpad.viewcontroller.option.profile;

import java.util.ArrayList;
import java.util.List;

import org.controlsfx.control.TaskProgressView;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.IProfileReloadTask;
import de.tobias.playpad.viewcontroller.option.IProfileSettingsViewController;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Worker;
import javafx.beans.Observable;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ProfileSettingsViewController extends ViewController implements IProfileSettingsViewController {

	@FXML private TabPane tabPane;
	@FXML private ToggleButton lockedButton;
	@FXML private Button finishButton;

	protected List<ProfileSettingsTabViewController> tabs = new ArrayList<>();

	private Runnable onFinish;

	public ProfileSettingsViewController(Midi midiHandler, Screen currentScreen, Window owner, Project project, Runnable onFinish) {
		super("settingsView", "de/tobias/playpad/assets/view/option/profile/", null, PlayPadMain.getUiResourceBundle());
		this.onFinish = onFinish;

		boolean activePlayer = project.hasPlayedPlayers();

		addTab(new MappingTabViewController());
		addTab(new MidiTabViewController());
		addTab(new DesignTabViewController());
		addTab(new PlayerTabViewController());

		// Custom Tabs - Content Types
		for (String type : PlayPadPlugin.getRegistryCollection().getPadContents().getTypes()) {
			try {
				PadContentConnect component = PlayPadPlugin.getRegistryCollection().getPadContents().getComponent(type);
				ProfileSettingsTabViewController controller = component.getSettingsTabViewController(activePlayer);
				if (controller != null) {
					addTab(controller);
				}
			} catch (NoSuchComponentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

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
		for (ProfileSettingsTabViewController controller : tabs) {
			controller.loadSettings(profile);
		}
	}

	/**
	 * Speichert die Einstellungen der Tabs.
	 */
	private void saveTabs() {
		Profile profile = Profile.currentProfile();
		for (ProfileSettingsTabViewController controller : tabs) {
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
		for (ProfileSettingsTabViewController controller : tabs) {
			if (controller.validSettings() == false) {
				return false;
			}
		}

		saveTabs();
		if (onFinish != null)
			onFinish.run(); // Reload MainViewController Settings

		IMainViewController mainController = PlayPadMain.getProgramInstance().getMainViewController();
		Profile profile = Profile.currentProfile();
		Project project = PlayPadMain.getProgramInstance().getCurrentProject();

		showProgressDialog(profile.getProfileSettings(), project, mainController);

		return true;
	}

	private void showProgressDialog(ProfileSettings settings, Project project, IMainViewController mainController) {
		TaskProgressView<Task<Void>> taskView = new TaskProgressView<>();

		for (ProfileSettingsTabViewController controller : tabs) {
			if (controller instanceof IProfileReloadTask) {
				if (controller.needReload()) {
					Task<Void> task = ((IProfileReloadTask) controller).getTask(settings, project, mainController);
					taskView.getTasks().add(task);
					Worker.runLater(task);
				}
			}
		}

		if (!taskView.getTasks().isEmpty()) {
			// Run Listener
			PlayPadMain.getProgramInstance().getSettingsListener().forEach(l -> l.onChange(Profile.currentProfile()));

			Scene scene = new Scene(taskView);
			Stage stage = new Stage();
			taskView.getTasks().addListener((Observable observable) ->
			{
				if (taskView.getTasks().isEmpty()) {
					stage.close();
				}
			});
			stage.setScene(scene);
			stage.showAndWait();
		}
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

	@Override
	public void addTab(ProfileSettingsTabViewController controller) {
		tabs.add(controller);
		tabPane.getTabs().add(new Tab(controller.name(), controller.getParent()));
	}

	public List<ProfileSettingsTabViewController> getTabs() {
		return tabs;
	}
}
