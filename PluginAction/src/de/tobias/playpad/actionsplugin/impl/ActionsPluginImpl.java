package de.tobias.playpad.actionsplugin.impl;

import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.dom4j.DocumentException;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.action.ActionFactory;
import de.tobias.playpad.actionsplugin.ActionsPlugin;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.WindowListener;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileListener;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.updater.client.Updatable;
import de.tobias.utils.ui.HUD;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.ui.icon.MaterialDesignIcon;
import de.tobias.utils.util.Localization;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.events.Shutdown;

@PluginImplementation
public class ActionsPluginImpl implements ActionsPlugin, ChangeListener<Boolean>, ProfileListener {

	private static final String NAME = "ActionsPlugin";
	private static final String IDENTIFIER = "de.tobias.playpad.actions.impl.ActionsPluginImpl";

	private static Module module;
	private static ActionsPluginUpdater updater;
	
	private static ResourceBundle bundle;
	public static CheckMenuItem muteMenuItem;

	private static BooleanProperty muteProperty;
	private static ChangeListener<Number> volumeListener;

	private HUD muteHUD;

	@PluginLoaded
	public void onEnable(ActionsPlugin plugin) {
		muteProperty = new SimpleBooleanProperty();
		try {
			bundle = Localization.loadBundle("de/tobias/playpad/actionsplugin/assets/actions", ActionsPluginImpl.class.getClassLoader());
		} catch (MissingResourceException e) {
			System.err.println("Cannot find resource for actions plugin: " + e.getLocalizedMessage());
		}

		if (muteHUD == null) {
			Platform.runLater(() ->
			{
				FontIcon icon = new FontIcon(MaterialDesignIcon.FONT_FILE, MaterialDesignIcon.VOLUME_OFF);
				icon.setSize(60);
				icon.getStyleClass().remove(FontIcon.STYLE_CLASS);
				icon.setColor(Color.WHITE);
				icon.setAlignment(Pos.CENTER);
				muteHUD = new HUD(icon);
				muteHUD.setMinWidth(200);
				muteHUD.setMinHeight(100);
			});

			ChangeListener<Boolean> listener = (a, b, c) ->
			{
				if (c && volume == -1 && Profile.currentProfile().getProfileSettings().getVolume() != 0) {
					volume = Profile.currentProfile().getProfileSettings().getVolume();
				} else {
					volume = -1;
				}
			};

			PlayPadPlugin.getImplementation().addMainViewListener(new WindowListener<IMainViewController>() {

				@Override
				public void onInit(IMainViewController t) {
					t.performLayoutDependendAction((oldToolbar, newToolbar) ->
					{
						if (oldToolbar != null)
							oldToolbar.getVolumeSlider().valueChangingProperty().removeListener(listener);
						newToolbar.getVolumeSlider().valueChangingProperty().addListener(listener);
					});
				}
			});

			volumeListener = (a, b, c) ->
			{
				double newVolume = c.doubleValue();
				double oldVolume = b.doubleValue();

				if (newVolume != 0 && oldVolume == 0 && muteProperty.get()) {
					muteProperty.set(false);
				} else if (newVolume == 0 && oldVolume != 0 && !muteProperty.get()) {
					muteProperty.set(true);
				}
			};
		}

		Profile.registerListener(this);
		module = new Module(NAME, IDENTIFIER);
		updater = new ActionsPluginUpdater();

		try {
			Registry<ActionFactory> padContents = PlayPadPlugin.getRegistryCollection().getActions();
			padContents.loadComponentsFromFile("de/tobias/playpad/actionsplugin/assets/Actions.xml", getClass().getClassLoader(), module, bundle);
		} catch (Exception e) {
			e.printStackTrace();
		}
		muteProperty.addListener(this);

		System.out.println("Enable Action Plugin");

	}

	@Override
	public void reloadSettings(Profile oldProfile, Profile currentProfile) {
		if (oldProfile != null) {
			oldProfile.getProfileSettings().volumeProperty().removeListener(volumeListener);
		}
		currentProfile.getProfileSettings().volumeProperty().addListener(volumeListener);
	}

	public static BooleanProperty muteProperty() {
		return muteProperty;
	}

	@Shutdown
	public void onDisable() {
		System.out.println("Disable Action Plugin");
	}

	public static ResourceBundle getBundle() {
		return bundle;
	}

	private double volume = -1;

	@Override
	public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		Platform.runLater(() ->
		{
			if (newValue) {
				if (volume == -1) {
					volume = Profile.currentProfile().getProfileSettings().getVolume();
				}
				Profile.currentProfile().getProfileSettings().setVolume(0);

				Pane root = (Pane) PlayPadPlugin.getImplementation().getMainViewController().getParent();
				if (muteHUD != null)
					muteHUD.addToParent(root);
			} else {
				if (volume == 0) {
					volume = 1;
				}
				Profile.currentProfile().getProfileSettings().setVolume(volume);

				muteHUD.removeFromParent();
				volume = -1;
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
