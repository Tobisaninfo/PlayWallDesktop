package de.tobias.playpad.viewcontroller.option.profile;

import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;
import de.tobias.playpad.design.modern.model.ModernGlobalDesign;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.design.ModernGlobalDesignViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.IProfileReloadTask;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class DesignTabViewController extends ProfileSettingsTabViewController implements IProfileReloadTask {

	@FXML
	private VBox layoutContainer;

	DesignTabViewController() {
		load("view/option/profile", "LayoutTab", Localization.getBundle());

		ModernGlobalDesign design = Profile.currentProfile().getProfileSettings().getDesign();
		ModernGlobalDesignViewController globalLayoutViewController = new ModernGlobalDesignViewController(design);
		layoutContainer.getChildren().setAll(globalLayoutViewController.getParent());
	}

	@Override
	public void init() {
	}

	@Override
	public void loadSettings(Profile profile) {
	}

	@Override
	public void saveSettings(Profile profile) {
	}

	@Override
	public boolean needReload() {
		return true;
	}

	@Override
	public Runnable getTask(ProfileSettings settings, Project project, IMainViewController controller) {
		return () -> Platform.runLater(controller::loadUserCss);
	}

	@Override
	public boolean validSettings() {
		return true;
	}

	@Override
	public String name() {
		return Localization.getString(Strings.UI_WINDOW_SETTINGS_LAYOUT_TITLE);
	}
}
