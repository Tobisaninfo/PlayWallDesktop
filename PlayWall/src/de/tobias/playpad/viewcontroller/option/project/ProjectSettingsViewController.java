package de.tobias.playpad.viewcontroller.option.project;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.IProjectReloadTask;
import de.tobias.playpad.viewcontroller.option.IProjectSettingsViewController;
import de.tobias.playpad.viewcontroller.option.ProjectSettingsTabViewController;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;

public class ProjectSettingsViewController extends NVC implements IProjectSettingsViewController {

	@FXML private TabPane tabPane;
	@FXML private ToggleButton lockedButton;
	@FXML private Button finishButton;

	private List<ProjectSettingsTabViewController> tabs = new ArrayList<>();
	private Project project;

	private Runnable onFinish;

	public ProjectSettingsViewController(Screen currentScreen, Window owner, Project project, Runnable onFinish) {
		load("de/tobias/playpad/assets/view/option/project/", "projectSettingsView", PlayPadMain.getUiResourceBundle());
		this.onFinish = onFinish;
		this.project = project;

		boolean activePlayer = project.hasActivePlayers();

		addTab(new GeneralTabViewController(currentScreen, this, activePlayer));
		addTab(new PathsTabViewController());

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);
		nvcStage.addCloseHook(this::onFinish);
		addCloseKeyShortcut(() -> finishButton.fire());

		// Show Current Settings
		loadTabs(project.getSettings());
	}

	@Override
	public void init() {
		finishButton.defaultButtonProperty().bind(finishButton.focusedProperty());
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(715);
		stage.setMinHeight(500);
		stage.setTitle(Localization.getString(Strings.UI_Window_ProjectSettings_Title));

		Profile.currentProfile().currentLayout().applyCss(stage);
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

	// Button Listener
	@FXML
	private void finishButtonHandler(ActionEvent event) {
		onFinish();
		getStageContainer().ifPresent(NVCStage::close);
	}

	/**
	 * Speichert alle Informationen.
	 *
	 * @return <code>true</code>Alle Einstellungen sind Valid.
	 */
	private boolean onFinish() {
		for (ProjectSettingsTabViewController controller : tabs) {
			if (!controller.validSettings()) {
				return false;
			}
		}

		saveTabs(project.getSettings());
		if (onFinish != null)
			onFinish.run(); // Reload MainViewController Settings

		IMainViewController mainController = PlayPadMain.getProgramInstance().getMainViewController();
		executeConfigurationTasks(project.getSettings(), project, mainController);

		return true;
	}

	private void executeConfigurationTasks(ProjectSettings settings, Project project, IMainViewController mainController) {
		for (ProjectSettingsTabViewController controller : tabs) {
			if (controller instanceof IProjectReloadTask) {
				if (controller.needReload()) {
					Runnable task = ((IProjectReloadTask) controller).getTask(settings, project, mainController);
					Worker.runLater(task);
				}
			}
		}
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
