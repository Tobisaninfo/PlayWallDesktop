package de.tobias.playpad.awakeplugin;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.plugins.PluginArtifact;
import de.thecodelabs.plugins.PluginDescriptor;
import de.thecodelabs.utils.application.system.NativeApplication;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.plugin.MainWindowListener;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.PlayPadPluginStub;
import de.tobias.playpad.plugin.SettingsListener;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class AwakePluginImpl implements PlayPadPluginStub, PluginArtifact, MainWindowListener, EventHandler<ActionEvent>, SettingsListener {

	private Module module;

	private static final String SETTINGS_FILENAME = "Awake.xml";

	private CheckMenuItem activeMenu;
	private FontIcon toolbarIcon;

	private AwakeSettings settings = new AwakeSettings();

	@Override
	public void startup(PluginDescriptor descriptor) {
		Localization.addResourceBundle("lang/awake", getClass().getClassLoader());

		module = new Module(descriptor.getName(), descriptor.getArtifactId());

		PlayPadPlugin.getInstance().addMainViewListener(this);
		PlayPadPlugin.getInstance().addSettingsListener(this);
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
			Logger.error(e);
		}
	}

	@Override
	public void onInit(IMainViewController viewController) {
		activeMenu = new CheckMenuItem();
		activeMenu.setOnAction(this);
		activeMenu.setText(Localization.getString("plugin.awake.menu_item"));
		activeMenu.setSelected(settings.active);

		viewController.performLayoutDependedAction((oldToolbar, newToolbar) ->
		{
			if (oldToolbar != null)
				oldToolbar.removeMenuItem(activeMenu);
			newToolbar.addMenuItem(activeMenu, MenuType.EXTENSION);

			if (toolbarIcon != null) {
				if (settings.active) {
					if (oldToolbar != null)
						oldToolbar.removeToolbarItem(toolbarIcon);
					newToolbar.addToolbarItem(toolbarIcon);
				} else {
					newToolbar.removeToolbarItem(toolbarIcon);
				}
			}
		});
		toolbarIcon = new FontIcon(FontAwesomeType.MOON_ALT);
	}

	@Override
	public void handle(ActionEvent event) {
		activeSleep(activeMenu.isSelected());
		settings.active = activeMenu.isSelected();

		MenuToolbarViewController toolbarController = PlayPadPlugin.getInstance().getMainViewController().getMenuToolbarController();
		if (settings.active) {
			toolbarController.addToolbarItem(toolbarIcon);
		} else {
			toolbarController.removeToolbarItem(toolbarIcon);
		}
	}

	private void activeSleep(boolean activate) {
		NativeApplication.sharedInstance().preventSystemSleep(activate);
	}

	@Override
	public Module getModule() {
		return module;
	}

}