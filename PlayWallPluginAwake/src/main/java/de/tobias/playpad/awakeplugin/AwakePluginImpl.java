package de.tobias.playpad.awakeplugin;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.system.NativeApplication;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.plugin.*;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;
import de.tobias.updater.client.Updatable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class AwakePluginImpl implements AdvancedPlugin, WindowListener<IMainViewController>, EventHandler<ActionEvent>, SettingsListener {

	private static final String NAME = "AwakePlugin";
	private static final String IDENTIFIER = "de.tobias.playwall.plugin.awake";
	private static final int currentBuild = 3;
	private static final String currentVersion = "2.1";

	private Module module;
	private Updatable updatable;

	private static final String SETTINGS_FILENAME = "Awake.xml";

	private CheckMenuItem activeMenu;
	private Label iconLabel;

	private AwakeSettings settings = new AwakeSettings();

	private ResourceBundle bundle;

	@Override
	public void startup() {
		bundle = Localization.loadBundle("lang/awake", getClass().getClassLoader());

		module = new Module(NAME, IDENTIFIER);
		updatable = new StandardPluginUpdater(currentBuild, currentVersion, module);

		PlayPadPlugin.getImplementation().addMainViewListener(this);
		PlayPadPlugin.getImplementation().addSettingsListener(this);
		Logger.info("Enable Awake Plugin");
	}

	@Override
	public void shutdown() {
		Logger.info("Deactivate sleep prevention for shutdown");
		activeSleep(false); // Disable for shutdown
		Logger.info("Disable Awake Plugin");
	}

	@Override
	public void onLoad(Profile profile) {
		Path path = profile.getRef().getCustomFilePath(SETTINGS_FILENAME);

		try {
			settings = AwakeSettings.load(path);
		} catch (NoSuchFileException e) {
			Logger.info("No Awake.xml config in folder");
		} catch (DocumentException | IOException e) {
			Logger.error(e);
		}

		activeSleep(settings.active);
		if (activeMenu != null)
			activeMenu.setSelected(settings.active);
	}

	@Override
	public void onSave(Profile profile) {
		Path path = profile.getRef().getCustomFilePath(SETTINGS_FILENAME);

		try {
			settings.save(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onInit(IMainViewController t) {
		activeMenu = new CheckMenuItem();
		activeMenu.setOnAction(this);
		activeMenu.setText(bundle.getString("menutitle"));
		activeMenu.setSelected(settings.active);

		t.performLayoutDependedAction((oldToolbar, newToolbar) ->
		{
			if (oldToolbar != null)
				oldToolbar.removeMenuItem(activeMenu);
			newToolbar.addMenuItem(activeMenu, MenuType.EXTENSION);

			if (iconLabel != null) {
				if (settings.active) {
					if (oldToolbar != null)
						oldToolbar.removeToolbarItem(iconLabel);
					newToolbar.addToolbarItem(iconLabel);
				} else {
					newToolbar.removeToolbarItem(iconLabel);
				}
			}
		});
		iconLabel = new Label();
		iconLabel.setGraphic(new FontIcon(FontAwesomeType.MOON_ALT));
	}

	@Override
	public void handle(ActionEvent event) {
		activeSleep(activeMenu.isSelected());
		settings.active = activeMenu.isSelected();

		MenuToolbarViewController toolbarController = PlayPadPlugin.getImplementation().getMainViewController().getMenuToolbarController();
		if (settings.active) {
			toolbarController.addToolbarItem(iconLabel);
		} else {
			toolbarController.removeToolbarItem(iconLabel);
		}
	}

	private void activeSleep(boolean activate) {
		NativeApplication.sharedInstance().preventSystemSleep(activate);
	}

	@Override
	public Module getModule() {
		return module;
	}

	@Override
	public Updatable getUpdatable() {
		return updatable;
	}
}
