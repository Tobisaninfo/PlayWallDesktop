package de.tobias.playpad.viewcontroller.option.global;

import java.util.ArrayList;
import java.util.List;

import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import org.controlsfx.control.TaskProgressView;

import de.tobias.playpad.PlayPadImpl;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.GlobalSettingsTabViewController;
import de.tobias.playpad.viewcontroller.option.IGlobalReloadTask;
import de.tobias.playpad.viewcontroller.option.IGlobalSettingsViewController;
import de.tobias.playpad.viewcontroller.option.profile.GeneralTabViewController;
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
import javafx.stage.Stage;
import javafx.stage.Window;

public class GlobalSettingsViewController extends NVC implements IGlobalSettingsViewController {

	@FXML private TabPane tabPane;
	@FXML private ToggleButton lockedButton;
	@FXML private Button finishButton;

	private List<GlobalSettingsTabViewController> tabs = new ArrayList<>();

	private Runnable onFinish;

	public GlobalSettingsViewController(Window owner, Runnable onFinish) {
		load("de/tobias/playpad/assets/view/option/global/", "globalSettingsView",  PlayPadMain.getUiResourceBundle());
		this.onFinish = onFinish;

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);
		nvcStage.addCloseHook(this::onFinish);
		addCloseKeyShortcut(() -> finishButton.fire());

		addTab(new GeneralTabViewController(this));
		addTab(new KeysTabViewController());
		addTab(new UpdateTabViewController());

		// Show Current Settings
		loadTabs();
	}

	@Override
	public void init() {
		finishButton.defaultButtonProperty().bind(finishButton.focusedProperty());
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(715);
		stage.setMinHeight(700);
		stage.setTitle(Localization.getString(Strings.UI_Window_GlobalSettings_Title));

		Profile.currentProfile().currentLayout().applyCss(stage);
	}

	/**
	 * Zeigt die aktuellen Einstellungen für die Tabs an.
	 */
	private void loadTabs() {
		GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();

		for (GlobalSettingsTabViewController controller : tabs) {
			controller.loadSettings(globalSettings);
		}
	}

	/**
	 * Speichert die Einstellungen der Tabs.
	 */
	private void saveTabs() {
		GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();

		for (GlobalSettingsTabViewController controller : tabs) {
			controller.saveSettings(globalSettings);
		}

		try {
			globalSettings.save();
		} catch (Exception e) {
			showErrorMessage(Localization.getString(Strings.Error_Profile_Save, e.getLocalizedMessage()));
			e.printStackTrace();
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
		for (GlobalSettingsTabViewController controller : tabs) {
			if (!controller.validSettings()) {
				return false;
			}
		}

		saveTabs();
		if (onFinish != null)
			onFinish.run(); // Reload MainViewController Settings

		PlayPadImpl programInstance = PlayPadMain.getProgramInstance();
		IMainViewController mainController = programInstance.getMainViewController();
		GlobalSettings settings = programInstance.getGlobalSettings();
		showProgressDialog(settings, mainController);

		return true;
	}

	private void showProgressDialog(GlobalSettings settings, IMainViewController mainController) {
		TaskProgressView<Task<Void>> taskView = new TaskProgressView<>();

		for (GlobalSettingsTabViewController controller : tabs) {
			if (controller instanceof IGlobalReloadTask) {
				if (controller.needReload()) {
					Task<Void> task = ((IGlobalReloadTask) controller).getTask(settings, mainController);
					taskView.getTasks().add(task);
					Worker.runLater(task);
				}
			}
		}

		if (!taskView.getTasks().isEmpty()) {
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

	@Override
	public void addTab(GlobalSettingsTabViewController controller) {
		tabs.add(controller);
		tabPane.getTabs().add(new Tab(controller.name(), controller.getParent()));
	}

	public List<GlobalSettingsTabViewController> getTabs() {
		return tabs;
	}
}
