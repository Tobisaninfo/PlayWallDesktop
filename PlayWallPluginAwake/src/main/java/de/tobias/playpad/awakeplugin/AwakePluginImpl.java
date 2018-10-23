package de.tobias.playpad.awakeplugin;

import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.application.system.NativeApplication;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.OS;
import de.thecodelabs.utils.util.OS.OSType;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.plugin.*;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;
import de.tobias.updater.client.Updatable;
import de.tobias.updater.client.UpdateChannel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.events.Shutdown;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ResourceBundle;

@PluginImplementation
public class AwakePluginImpl implements AwakePlugin, WindowListener<IMainViewController>, EventHandler<ActionEvent>, SettingsListener {

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

	@PluginLoaded
	public void onLoad(AwakePlugin plugin) {
		bundle = Localization.loadBundle("lang/awake", getClass().getClassLoader());

		module = new Module(NAME, IDENTIFIER);
		updatable = new StandardPluginUpdater(currentBuild, currentVersion, module);

		if (OS.getType() == OSType.Windows) {
			try {
				loadJNA();
				ModernPluginManager.getInstance().loadFile(ApplicationUtils.getApplication().getPath(PathType.LIBRARY, "jna"));
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

	private void loadJNA() throws IOException {
		Path folder = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, "jna");

		if (Files.notExists(folder)) {
			Files.createDirectories(folder);
		}

		Path jnaFile = folder.resolve("jna.jar");
		Path jnaPlatformFile = folder.resolve("jna-platform.jar");

		if (Files.notExists(jnaFile)) {
			System.out.println("Download: /plugins/libAwake/jna.jar");
			PlayPadPlugin.getServerHandler().getServer().loadSource("/plugins/libAwake/jna.jar", UpdateChannel.STABLE, jnaFile);
		}

		if (Files.notExists(jnaPlatformFile)) {
			System.out.println("Download: /plugins/libAwake/jna-platform.jar");
			PlayPadPlugin.getServerHandler().getServer().loadSource("/plugins/libAwake/jna-platform.jar", UpdateChannel.STABLE, jnaPlatformFile);
		}
	}

	@Override
	public void onLoad(Profile profile) {
		Path path = profile.getRef().getCustomFilePath(SETTINGS_FILENAME);

		try {
			settings = AwakeSettings.load(path);
		} catch (NoSuchFileException e) {
			System.out.println("No Awake.xml config in folder");
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
