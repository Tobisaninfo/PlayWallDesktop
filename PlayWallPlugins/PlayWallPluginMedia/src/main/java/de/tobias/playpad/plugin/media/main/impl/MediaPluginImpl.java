package de.tobias.playpad.plugin.media.main.impl;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.midi.action.ActionRegistry;
import de.thecodelabs.plugins.PluginDescriptor;
import de.thecodelabs.plugins.versionizer.PluginArtifact;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.scene.HUD;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.action.ActionProvider;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.PlayPadPluginStub;
import de.tobias.playpad.plugin.SettingsListener;
import de.tobias.playpad.plugin.media.action.BlackAction;
import de.tobias.playpad.plugin.media.main.VideoSettings;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.registry.Registry;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class MediaPluginImpl implements PlayPadPluginStub, PluginArtifact, SettingsListener, ChangeListener<Boolean> {

	private static Module module;

	private static MediaPluginImpl instance;
	private MediaViewController videoViewController;
	private VideoSettings settings = new VideoSettings();

	private HUD blindHUD;
	private static BooleanProperty blindProperty;

	private static final String SETTINGS_FILENAME = "Media.xml";

	@Override
	public void startup(PluginDescriptor descriptor) {
		// Init
		MediaPluginImpl.instance = this;
		MediaPluginImpl.module = new Module(descriptor.getName(), descriptor.getArtifactId());

		MediaPluginImpl.blindProperty = new SimpleBooleanProperty();

		// Load Content Types
		Localization.addResourceBundle("lang/video", getClass().getClassLoader());
		try {
			Registry<PadContentFactory> padContents = PlayPadPlugin.getRegistries().getPadContents();
			padContents.loadComponentsFromFile("PadContent.xml", getClass().getClassLoader(), module, Localization.getBundle());

			ActionRegistry.registerActionHandler(new BlackAction());
		} catch (Exception e) {
			Logger.error(e);
		}

		PlayPadPlugin.getInstance().addSettingsListener(this);
		if (Profile.currentProfile() != null) {
			onLoad(Profile.currentProfile());
			onChange(Profile.currentProfile());
		}

		Platform.runLater(() -> {
			videoViewController = new MediaViewController(settings);

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
		});

		try {
			Registry<ActionProvider> padContents = PlayPadPlugin.getRegistries().getActions();
			padContents.loadComponentsFromFile("Actions.xml", getClass().getClassLoader(), module, Localization.getBundle());
		} catch (Exception e) {
			Logger.error(e);
		}
		blindProperty.addListener(this);

		Logger.debug("Enable Media Plugin");
	}

	@Override
	public void shutdown() {
		Platform.runLater(() -> {
			videoViewController.getStage().setFullScreen(false);
			videoViewController.getStage().close();
		});
		Logger.debug("Disable Media Plugin");
	}

	@Override
	public void onLoad(Profile profile) {
		Path path = profile.getRef().getCustomFilePath(SETTINGS_FILENAME);

		try {
			settings.load(path);
		} catch (NoSuchFileException e) {
			Logger.debug("No Media.xml config on folder");
		} catch (DocumentException | IOException e) {
			Logger.error(e);
		}
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

	public VideoSettings getCurrentSettings() {
		return settings;
	}

	public static MediaPluginImpl getInstance() {
		return instance;
	}

	public MediaViewController getVideoViewController() {
		return videoViewController;
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
				Pane root = (Pane) PlayPadPlugin.getInstance().getMainViewController().getParent();
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

}
