package de.tobias.playpad.equalizerplugin.impl;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.equalizerplugin.Equalizer;
import de.tobias.playpad.equalizerplugin.EqualizerPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.play.Equalizeable;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.PadListener;
import de.tobias.playpad.plugin.StandardPluginUpdater;
import de.tobias.playpad.plugin.WindowListener;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.updater.client.Updatable;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.media.AudioEqualizer;
import javafx.scene.media.EqualizerBand;
import javafx.stage.Stage;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.events.Shutdown;
import org.dom4j.DocumentException;

import java.io.IOException;
import java.util.ResourceBundle;

@PluginImplementation
public class EqualizerPluginImpl implements EqualizerPlugin, WindowListener<IMainViewController>, EventHandler<ActionEvent>, PadListener {

	private static final String NAME = "Equalizer";
	private static final String IDENTIFIER = "de.tobias.playwall.plugin.equalizer";
	private static final int currentBuild = 5;
	private static final String currentVersion = "4.1";

	private Module module;
	private Updatable updatable;

	private Stage mainStage;
	private EqualizerViewController equalizerViewController;
	private static ResourceBundle bundle;

	private MenuItem eqMenuItem;

	static ResourceBundle getBundle() {
		return bundle;
	}

	@PluginLoaded
	public void onEnable(EqualizerPlugin plugin) {
		bundle = Localization.loadBundle("lang/equalizer", EqualizerPluginImpl.class.getClassLoader());
		try {
			Equalizer.load(ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "equalizer.xml"));
		} catch (DocumentException | IOException e) {
			e.printStackTrace();
		}

		module = new Module(NAME, IDENTIFIER);
		updatable = new StandardPluginUpdater(currentBuild, currentVersion, module);

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

		t.performLayoutDependedAction((oldToolbar, newToolbar) ->
		{
			if (oldToolbar != null)
				oldToolbar.removeMenuItem(eqMenuItem);
			newToolbar.addMenuItem(eqMenuItem, MenuType.EXTENSION);
		});
	}

	@Override
	public void onPlay(Pad pad) {
		PadContent content = pad.getContent();
		if (content instanceof Equalizeable) {

			// Equalizer
			Equalizeable equalizeable = (Equalizeable) content;
			AudioEqualizer audioEqualizer = equalizeable.getAudioEqualizer();
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
		if (content instanceof Equalizeable) {

			// Equalizer
			Equalizeable equalizeable = (Equalizeable) content;
			AudioEqualizer audioEqualizer = equalizeable.getAudioEqualizer();
			if (audioEqualizer != null) {
				for (EqualizerBand band : audioEqualizer.getBands()) {
					band.gainProperty().unbind();
				}
				audioEqualizer.enabledProperty().unbind();
			}
		}
	}

	@Override
	public void handle(ActionEvent event) {
		if (equalizerViewController == null) {
			equalizerViewController = new EqualizerViewController(mainStage);
			equalizerViewController.getStageContainer().ifPresent(NVCStage::show);
		} else if (equalizerViewController.getContainingWindow().isShowing()) {
			equalizerViewController.getStageContainer().ifPresent(s -> s.getStage().toFront());
		} else {
			equalizerViewController.getStageContainer().ifPresent(NVCStage::show);
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