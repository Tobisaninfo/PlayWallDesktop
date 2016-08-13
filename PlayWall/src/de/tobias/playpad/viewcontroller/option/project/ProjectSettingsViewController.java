package de.tobias.playpad.viewcontroller.option.project;

import java.util.ArrayList;
import java.util.List;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.option.IProjectSettingsViewController;
import de.tobias.playpad.viewcontroller.option.ProjectSettingsTabViewController;
import de.tobias.utils.ui.ViewController;
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

public class ProjectSettingsViewController extends ViewController implements IProjectSettingsViewController {

	@FXML private TabPane tabPane;
	@FXML private ToggleButton lockedButton;
	@FXML private Button finishButton;

	protected List<ProjectSettingsTabViewController> tabs = new ArrayList<>();
	private ProjectSettings settings;

	private Runnable onFinish;

	public ProjectSettingsViewController(Screen currentScreen, Window owner, Project project, Runnable onFinish) {
		super("projectSettingsView", "de/tobias/playpad/assets/view/option/project/", null, PlayPadMain.getUiResourceBundle());
		this.onFinish = onFinish;
		this.settings = project.getSettings();

		getStage().initOwner(owner);
		boolean activePlayer = project.hasPlayedPlayers();

		addTab(new GeneralTabViewController(currentScreen, this, activePlayer));
		addTab(new PathsTabViewController());

		// Show Current Settings
		loadTabs(settings);
	}

	@Override
	public void init() {
		// KeyCode
		addCloseKeyShortcut(() -> finishButton.fire());

		finishButton.defaultButtonProperty().bind(finishButton.focusedProperty());
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(715);
		stage.setMinHeight(500);
		stage.setTitle(Localization.getString(Strings.UI_Window_GlobalSettings_Title));

		Profile.currentProfile().currentLayout().applyCss(getStage());
	}

	/**
	 * Zeigt die aktuellen Einstellungen für die Tabs an.
	 */
	private void loadTabs(ProjectSettings settings) {
		for (ProjectSettingsTabViewController controller : tabs) {
			controller.loadSettings(settings);
		}
	}

	/**
	 * Speichert die Einstellungen der Tabs.
	 */
	private void saveTabs(ProjectSettings settings) {
		for (ProjectSettingsTabViewController controller : tabs) {
			controller.saveSettings(settings);
		}
	}

	@Override
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
		for (ProjectSettingsTabViewController controller : tabs) {
			if (controller.validSettings() == false) {
				return false;
			}
		}

		saveTabs(settings);
		if (onFinish != null)
			onFinish.run(); // Reload MainViewController Settings
		return true;
	}

	@Override
	public void addTab(ProjectSettingsTabViewController controller) {
		tabs.add(controller);
		tabPane.getTabs().add(new Tab(controller.name(), controller.getParent()));
	}

	public List<ProjectSettingsTabViewController> getTabs() {
		return tabs;
	}
}
