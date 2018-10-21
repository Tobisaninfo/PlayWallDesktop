package de.tobias.playpad.viewcontroller.option.profile;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.design.modern.ModernGlobalDesign2;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.pad.content.PadContentRegistry;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.IProfileReloadTask;
import de.tobias.playpad.viewcontroller.option.IProfileSettingsViewController;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import de.tobias.utils.threading.Worker;
import de.tobias.utils.ui.NVC;
import de.tobias.utils.ui.NVCStage;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;

public class ProfileSettingsViewController extends NVC implements IProfileSettingsViewController {

	@FXML
	private TabPane tabPane;
	@FXML
	private ToggleButton lockedButton;
	@FXML
	private Button finishButton;

	private List<ProfileSettingsTabViewController> tabs = new ArrayList<>();

	private Runnable onFinish;

	public ProfileSettingsViewController(Window owner, Project project, Runnable onFinish) {
		load("view/option/profile", "SettingsView", PlayPadMain.getUiResourceBundle());
		this.onFinish = onFinish;

		boolean activePlayer = project.hasActivePlayers();

		addTab(new MappingTabViewController());
		addTab(new DesignTabViewController());
		addTab(new PlayerTabViewController());

		// Custom Tabs - Content Types
		PadContentRegistry padContents = PlayPadPlugin.getRegistryCollection().getPadContents();
		for (String type : padContents.getTypes()) {
			PadContentFactory component = padContents.getFactory(type);
			ProfileSettingsTabViewController controller = component.getSettingsTabViewController(activePlayer);
			if (controller != null) {
				addTab(controller);
			}
		}

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);
		nvcStage.addCloseHook(this::onFinish);
		addCloseKeyShortcut(() -> finishButton.fire());

		// Show Current Settings
		loadTabs();
	}

	@Override
	public void init() {
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();

		// Lock Button Listener
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

		ModernGlobalDesign2 design = Profile.currentProfile().getProfileSettings().getDesign();
		PlayPadPlugin.getModernDesignHandler().getModernGlobalDesignHandler().applyCss(design, stage);
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

	// Button Listener
	@FXML
	private void finishButtonHandler(ActionEvent event) {
		if (onFinish()) {
			getStageContainer().ifPresent(NVCStage::close);
		}
	}

	/**
	 * Speichert alle Informationen.
	 *
	 * @return <code>true</code>Alle Einstellungen sind Valid.
	 */
	private boolean onFinish() {
		for (ProfileSettingsTabViewController controller : tabs) {
			if (!controller.validSettings()) {
				return false;
			}
		}

		saveTabs();
		if (onFinish != null)
			onFinish.run(); // Reload MainViewController Settings

		IMainViewController mainController = PlayPadMain.getProgramInstance().getMainViewController();
		Profile profile = Profile.currentProfile();
		Project project = PlayPadMain.getProgramInstance().getCurrentProject();

		executeConfigurationTasks(profile.getProfileSettings(), project, mainController);

		return true;
	}

	private void executeConfigurationTasks(ProfileSettings settings, Project project, IMainViewController mainController) {
		for (ProfileSettingsTabViewController controller : tabs) {
			if (controller instanceof IProfileReloadTask) {
				if (controller.needReload()) {
					Runnable task = ((IProfileReloadTask) controller).getTask(settings, project, mainController);
					Worker.runLater(task);
				}
			}
		}
	}

	/**
	 * Aktiviert/Deaktiviert den Look Button.
	 *
	 * @param isLocked isLooked
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
