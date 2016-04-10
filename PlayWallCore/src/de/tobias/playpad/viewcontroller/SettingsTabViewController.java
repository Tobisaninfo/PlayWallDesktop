package de.tobias.playpad.viewcontroller;

import java.util.ResourceBundle;

import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.ui.ContentViewController;

public abstract class SettingsTabViewController extends ContentViewController {

	public SettingsTabViewController(String name, String path, ResourceBundle localization) {
		super(name, path, localization);
	}

	public abstract void loadSettings(Profile profile);

	public abstract void saveSettings(Profile profile);

	public abstract boolean needReload();

	public void reload(Profile profile, Project project, IMainViewController controller) {}

	public abstract boolean validSettings();

	public abstract String name();
}
