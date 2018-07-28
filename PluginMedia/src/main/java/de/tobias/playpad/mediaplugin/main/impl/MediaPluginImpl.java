package de.tobias.playpad.mediaplugin.main.impl;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.action.ActionFactory;
import de.tobias.playpad.mediaplugin.main.MediaPlugin;
import de.tobias.playpad.mediaplugin.main.VideoSettings;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.SettingsListener;
import de.tobias.playpad.plugin.StandardPluginUpdater;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.registry.Registry;
import de.tobias.updater.client.Updatable;
import de.tobias.utils.nui.HUD;
import de.tobias.utils.nui.icon.FontAwesomeType;
import de.tobias.utils.nui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.events.Shutdown;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ResourceBundle;

@PluginImplementation
public class MediaPluginImpl implements MediaPlugin, SettingsListener, ChangeListener<Boolean> {

	private static final String NAME = "MediaPlugin";
	private static final String IDENTIFIER = "de.tobias.playwall.plugin.media";
	private static final int currentBuild = 8;
	private static final String currentVersion = "4.2.2";

	private static Module module;
	private static Updatable updater;

	private static MediaPluginImpl instance;
	private MediaViewController videoViewController;
	private VideoSettings settings = new VideoSettings();

	private ResourceBundle bundle;
	private HUD blindHUD;
	private static BooleanProperty blindProperty;

	private static final String SETTINGS_FILENAME = "Media.xml";

	@PluginLoaded
	public void onEnable(MediaPlugin plugin) {
		// Init
		instance = this;
		module = new Module(NAME, IDENTIFIER);
		updater = new StandardPluginUpdater(currentBuild, currentVersion, module);

		blindProperty = new SimpleBooleanProperty();

		bundle = Localization.loadBundle("de/tobias/playpad/mediaplugin/assets/video", getClass().getClassLoader());
		videoViewController = new MediaViewController(settings);

		// Load Content Types
		try {
			Registry<PadContentFactory> padContents = PlayPadPlugin.getRegistryCollection().getPadContents();
			padContents.loadComponentsFromFile("de/tobias/playpad/mediaplugin/assets/PadContent.xml", getClass().getClassLoader(), module, bundle);
		} catch (Exception e) {
			e.printStackTrace();
		}

		PlayPadPlugin.getImplementation().addSettingsListener(this);

		if (Profile.currentProfile() != null) {
			onLoad(Profile.currentProfile());
			onChange(Profile.currentProfile());
		}

		if (blindHUD == null) {
			Platform.runLater(() ->
			{
				FontIcon icon = new FontIcon(FontAwesomeType.DESKTOP);
				icon.setSize(60);
				icon.getStyleClass().remove(FontIcon.STYLE_CLASS);
				icon.setColor(Color.WHITE);
				icon.setAlignment(Pos.CENTER);
				blindHUD = new HUD(icon);
				blindHUD.setPosition(Pos.TOP_CENTER);
				blindHUD.setMinWidth(200);
				blindHUD.setMinHeight(100);
			});
		}

		try {
			Registry<ActionFactory> padContents = PlayPadPlugin.getRegistryCollection().getActions();
			padContents.loadComponentsFromFile("de/tobias/playpad/mediaplugin/assets/Actions.xml", getClass().getClassLoader(), module, bundle);
		} catch (Exception e) {
			e.printStackTrace();
		}
		blindProperty.addListener(this);

		System.out.println("Enable Media Plugin");
	}

	@Shutdown
	public void onDisable() {
		Platform.runLater(() -> {
			videoViewController.getStage().setFullScreen(false);
			videoViewController.getStage().close();
		});
		System.out.println("Disable Media Plugin");
	}

	@Override
	public void onLoad(Profile profile) {
		Path path = profile.getRef().getCustomFilePath(SETTINGS_FILENAME);

		try {
			settings.load(path);
		} catch (NoSuchFileException e) {
			System.out.println("No Awake.xml config on folder");
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
		}
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

	public VideoSettings getCurrentSettings() {
		return settings;
	}

	public static MediaPluginImpl getInstance() {
		return instance;
	}

	public MediaViewController getVideoViewController() {
		return videoViewController;
	}

	public ResourceBundle getBundle() {
		return bundle;
	}

	public static BooleanProperty blindProperty() {
		return blindProperty;
	}

	@Override
	public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		Platform.runLater(() ->
		{
			if (newValue) {
				videoViewController.blind(true);
				Pane root = (Pane) PlayPadPlugin.getImplementation().getMainViewController().getParent();
				if (blindHUD != null)
					blindHUD.addToParent(root);
			} else {
				videoViewController.blind(false);
				blindHUD.removeFromParent();
			}
		});
	}

	@Override
	public Module getModule() {
		return module;
	}

	@Override
	public Updatable getUpdatable() {
		return updater;
	}
}