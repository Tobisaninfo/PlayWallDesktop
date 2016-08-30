package de.tobias.playpad.awakeplugin.impl;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ResourceBundle;

import org.dom4j.DocumentException;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.awakeplugin.AwakePlugin;
import de.tobias.playpad.awakeplugin.AwakeSettings;
import de.tobias.playpad.plugin.SettingsListener;
import de.tobias.playpad.plugin.WindowListener;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.update.UpdateRegistery;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.AwakeUtils;
import de.tobias.utils.util.IOUtils;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.OS;
import de.tobias.utils.util.OS.OSType;
import de.tobias.utils.util.win.Kernel32;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.events.Shutdown;

@PluginImplementation
public class AwakePluginImpl implements AwakePlugin, WindowListener<IMainViewController>, EventHandler<ActionEvent>, SettingsListener {

	private static final String SETTINGS_FILENAME = "Awake.xml";

	private CheckMenuItem activeMenu;
	private Label iconLabel;

	private AwakeSettings settings = new AwakeSettings();

	private ResourceBundle bundle;

	@PluginLoaded
	public void onLoad(AwakePlugin plugin) {
		bundle = Localization.loadBundle("de/tobias/playpad/awakeplugin/assets/awake", getClass().getClassLoader());

		UpdateRegistery.registerUpdateable(new AwakePluginUpdater());

		if (OS.getType() == OSType.Windows) {
			try {
				loadJNA();
				PlayPadPlugin.getImplementation().loadPlugin(ApplicationUtils.getApplication().getPath(PathType.LIBRARY, "jna").toUri());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (OS.getType() == OSType.MacOSX) {
			try {
				Path file = loadLibMac();
				AwakeUtils.load(file.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		PlayPadPlugin.getImplementation().addMainViewListener(this);
		PlayPadPlugin.getImplementation().addSettingsListener(this);
		System.out.println("Enable Awake Plugin");
	}

	@Shutdown
	public void onDisable() {
		System.out.println("Disable Awake Plugin");
	}

	private Path loadLibMac() throws IOException {
		Path folder = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, "awakelib.dylib");
		if (Files.notExists(folder)) {
			Files.createFile(folder);
			URL url = new URL(ApplicationUtils.getApplication().getInfo().getUpdateURL() + "/plugins/libAwake/libAwakeLib.dylib");
			System.out.println("Downlaod " + url);
			IOUtils.copy(url.openStream(), folder);
		}
		return folder;
	}

	private void loadJNA() throws IOException {
		Path folder = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, "jna");

		Path jnaFile = folder.resolve("jna.jar");
		Path jnaPlatformFile = folder.resolve("jna-platform.jar");

		if (Files.notExists(jnaFile)) {
			Files.createDirectories(folder);
			URL url = new URL(ApplicationUtils.getApplication().getInfo().getUpdateURL() + "/plugins/jna/jna.jar");
			System.out.println("Downlaod " + url);
			IOUtils.copy(url.openStream(), jnaFile);
		}

		if (Files.notExists(jnaPlatformFile)) {
			Files.createDirectories(folder);
			URL url = new URL(ApplicationUtils.getApplication().getInfo().getUpdateURL() + "/plugins/jna/jna-platform.jar");
			System.out.println("Downlaod " + url);
			IOUtils.copy(url.openStream(), jnaPlatformFile);
		}
	}

	@Override
	public void onLoad(Profile profile) {
		Path path = profile.getRef().getCustomFilePath(SETTINGS_FILENAME);

		try {
			settings = AwakeSettings.load(path);
		} catch (NoSuchFileException e) {
			System.out.println("No Awake.xml config on folder");
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
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

		t.performLayoutDependendAction((oldToolbar, newToolbar) ->
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

	public void activeSleep(boolean activate) {
		if (activate) {
			if (OS.getType() == OSType.Windows) {
				Kernel32.INSTANCE.SetThreadExecutionState(Kernel32.ES_CONTINUOUS | Kernel32.ES_DISPLAY_REQUIRED | Kernel32.ES_SYSTEM_REQUIRED);
			} else if (OS.getType() == OSType.MacOSX) {
				AwakeUtils.getInstance().lock();
			}
		} else {
			if (OS.getType() == OSType.Windows) {
				Kernel32.INSTANCE.SetThreadExecutionState(Kernel32.ES_CONTINUOUS);
			} else if (OS.getType() == OSType.MacOSX) {
				AwakeUtils.getInstance().unlock();
			}
		}
	}
}
