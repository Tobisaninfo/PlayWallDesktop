package de.tobias.playpad.equalizerplugin.main.impl;

import java.io.IOException;
import java.util.ResourceBundle;

import org.dom4j.DocumentException;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.audio.Equalizable;
import de.tobias.playpad.equalizerplugin.main.Equalizer;
import de.tobias.playpad.equalizerplugin.main.EqualizerPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.PadListener;
import de.tobias.playpad.plugin.WindowListener;
import de.tobias.playpad.update.Updatable;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.media.AudioEqualizer;
import javafx.scene.media.EqualizerBand;
import javafx.stage.Stage;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.events.Shutdown;

@PluginImplementation
public class EqualizerPluginImpl implements EqualizerPlugin, WindowListener<IMainViewController>, EventHandler<ActionEvent>, PadListener {

	private static final String NAME = "Equalizer";
	private static final String IDENTIFIER = "de.tobias.playpad.videoplugin.main.impl.EqualizerPluginImpl";

	private Module module;
	private Updatable updatable;

	private Stage mainStage;
	private EqualizerViewController equalizerViewController;
	private static ResourceBundle bundle;

	private MenuItem eqMenuItem;

	public static ResourceBundle getBundle() {
		return bundle;
	}

	@PluginLoaded
	public void onEnable(EqualizerPlugin plugin) {
		bundle = Localization.loadBundle("de/tobias/playpad/equalizerplugin/assets/equalizer", EqualizerPluginImpl.class.getClassLoader());
		try {
			Equalizer.load(ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "equalizer.xml"));
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
		}

		updatable = new EqualizerPluginUpdater();
		module = new Module(NAME, IDENTIFIER);

		PlayPadPlugin.getImplementation().addMainViewListener(this);
		PlayPadPlugin.getImplementation().addPadListener(this);

		System.out.println("Enable Equalizer Plugin");
	}

	@Shutdown
	public void onDisable() {
		System.out.println("Disable Equalizer Plugin");
	}

	@Override
	public void onInit(IMainViewController t) {
		mainStage = t.getStage();

		eqMenuItem = new MenuItem();
		eqMenuItem.setText(bundle.getString("eq.menuitem.name"));
		eqMenuItem.setOnAction(this);

		t.performLayoutDependendAction((oldToolbar, newToolbar) ->
		{
			if (oldToolbar != null)
				oldToolbar.removeMenuItem(eqMenuItem);
			newToolbar.addMenuItem(eqMenuItem, MenuType.EXTENSION);
		});
	}

	@Override
	public void onPlay(Pad pad) {
		PadContent content = pad.getContent();
		if (content != null && content instanceof Equalizable) {

			// Equalizer
			Equalizable equalizable = (Equalizable) content;
			AudioEqualizer audioEqualizer = equalizable.getAudioEqualizer();
			if (audioEqualizer != null) {
				for (EqualizerBand band : audioEqualizer.getBands()) {
					band.gainProperty().bind(Equalizer.getInstance().gainProperty((int) band.getBandwidth()));
				}
				audioEqualizer.enabledProperty().bind(Equalizer.getInstance().enableProperty());
			}
		}
	}

	@Override
	public void onStop(Pad pad) {
		PadContent content = pad.getContent();
		if (content != null && content instanceof Equalizable) {

			// Equalizer
			Equalizable equalizable = (Equalizable) content;
			AudioEqualizer audioEqualizer = equalizable.getAudioEqualizer();
			if (audioEqualizer != null) {
				for (EqualizerBand band : audioEqualizer.getBands()) {
					band.gainProperty().bind(Equalizer.getInstance().gainProperty((int) band.getBandwidth()));
				}
				audioEqualizer.enabledProperty().bind(Equalizer.getInstance().enableProperty());
			}
		}
	}

	@FXML
	public void handle(ActionEvent event) {
		if (equalizerViewController == null) {
			equalizerViewController = new EqualizerViewController(mainStage);
			equalizerViewController.getStage().show();
		} else if (equalizerViewController.getStage().isShowing()) {
			equalizerViewController.getStage().toFront();
		} else {
			equalizerViewController.getStage().show();
		}
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