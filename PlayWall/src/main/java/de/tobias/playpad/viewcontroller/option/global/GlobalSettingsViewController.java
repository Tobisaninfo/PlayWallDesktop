package de.tobias.playpad.viewcontroller.option.global;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadImpl;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.GlobalSettingsTabViewController;
import de.tobias.playpad.viewcontroller.option.IGlobalReloadTask;
import de.tobias.playpad.viewcontroller.option.IGlobalSettingsViewController;
import de.tobias.playpad.viewcontroller.option.profile.GeneralTabViewController;
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

public class GlobalSettingsViewController extends NVC implements IGlobalSettingsViewController {

	@FXML
	private TabPane tabPane;
	@FXML
	private ToggleButton lockedButton;
	@FXML
	private Button finishButton;

	private final List<GlobalSettingsTabViewController> tabs = new ArrayList<>();
	private final Runnable onFinish;

	public GlobalSettingsViewController(Window owner, Runnable onFinish) {
		load("view/option/global", "GlobalSettingsView", Localization.getBundle());
		this.onFinish = onFinish;

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);
		nvcStage.addCloseHook(this::onFinish);
		addCloseKeyShortcut(() -> finishButton.fire());

		addTab(new GeneralTabViewController(this));
		addTab(new KeysTabViewController());
		addTab(new UpdateTabViewController());

		PlayPadPlugin.getInstance().getGlobalSettingsTabs().forEach(supplier -> {
			final GlobalSettingsTabViewController globalSettingsTabViewController = supplier.get();
			if (globalSettingsTabViewController != null) {
				addTab(globalSettingsTabViewController);
			}
		});

		// Show Current Settings
		loadTabs();
	}

	@Override
	public void init() {
		finishButton.defaultButtonProperty().bind(finishButton.focusedProperty());
	}

	@Override
	public void initStage(Stage stage) {
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

		stage.setMinWidth(715);
		stage.setMinHeight(800);
		stage.setTitle(Localization.getString(Strings.UI_WINDOW_GLOBAL_SETTINGS_TITLE));

		PlayPadPlugin.styleable().applyStyle(stage);
	}

	/**
	 * Zeigt die aktuellen Einstellungen für die Tabs an.
	 */
	private void loadTabs() {
		GlobalSettings globalSettings = PlayPadPlugin.getInstance().getGlobalSettings();

		for (GlobalSettingsTabViewController controller : tabs) {
			controller.loadSettings(globalSettings);
		}
	}

	/**
	 * Speichert die Einstellungen der Tabs.
	 */
	private void saveTabs() {
		GlobalSettings globalSettings = PlayPadPlugin.getInstance().getGlobalSettings();

		for (GlobalSettingsTabViewController controller : tabs) {
			controller.saveSettings(globalSettings);
		}

		try {
			globalSettings.save();
		} catch (Exception e) {
			showErrorMessage(Localization.getString(Strings.ERROR_PROFILE_SAVE, e.getLocalizedMessage()));
			Logger.error(e);
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
		executeConfigurationTasks(settings, mainController);

		return true;
	}

	private void executeConfigurationTasks(GlobalSettings settings, IMainViewController mainController) {
		for (GlobalSettingsTabViewController controller : tabs) {
			if (controller instanceof IGlobalReloadTask) {
				if (controller.needReload()) {
					Runnable task = ((IGlobalReloadTask) controller).getTask(settings, mainController);
					Worker.runLater(task);
				}
			}
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
