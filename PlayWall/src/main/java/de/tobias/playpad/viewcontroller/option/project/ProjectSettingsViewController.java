package de.tobias.playpad.viewcontroller.option.project;

import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.IProjectReloadTask;
import de.tobias.playpad.viewcontroller.option.IProjectSettingsViewController;
import de.tobias.playpad.viewcontroller.option.ProjectSettingsTabViewController;
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

	@FXML
	private TabPane tabPane;
	@FXML
	private ToggleButton lockedButton;
	@FXML
	private Button finishButton;

	private final List<ProjectSettingsTabViewController> tabs = new ArrayList<>();
	private final Project project;

	private final Runnable onFinish;

	public ProjectSettingsViewController(Screen currentScreen, Window owner, Project project, Runnable onFinish) {
		load("view/option/project", "ProjectSettingsView", Localization.getBundle());
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
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

		stage.setMinWidth(715);
		stage.setMinHeight(500);
		stage.setTitle(Localization.getString(Strings.UI_WINDOW_PROJECT_SETTINGS_TITLE));

		PlayPadPlugin.styleable().applyStyle(stage);
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
