package de.tobias.playpad.viewcontroller.option.global;

import java.util.ArrayList;
import java.util.List;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.option.GlobalSettingsTabViewController;
import de.tobias.playpad.viewcontroller.option.IGlobalSettingsViewController;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;
import javafx.stage.Window;

public class GlobalSettingsViewController extends ViewController implements IGlobalSettingsViewController {

	@FXML private TabPane tabPane;
	@FXML private ToggleButton lockedButton;
	@FXML private Button finishButton;

	protected List<GlobalSettingsTabViewController> tabs = new ArrayList<>();

	private Runnable onFinish;

	public GlobalSettingsViewController(Window owner, Runnable onFinish) {
		super("globalSettingsView", "de/tobias/playpad/assets/view/option/global/", null, PlayPadMain.getUiResourceBundle());
		this.onFinish = onFinish;

		getStage().initOwner(owner);

		addTab(new KeysTabViewController());
		addTab(new UpdateTabViewController());

		// Show Current Settings
		loadTabs();
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
		stage.setMinHeight(700);
		stage.setTitle(Localization.getString(Strings.UI_Window_GlobalSettings_Title));

		Profile.currentProfile().currentLayout().applyCss(getStage());
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
		for (GlobalSettingsTabViewController controller : tabs) {
			if (controller.validSettings() == false) {
				return false;
			}
		}

		saveTabs();
		if (onFinish != null)
			onFinish.run(); // Reload MainViewController Settings
		return true;
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
