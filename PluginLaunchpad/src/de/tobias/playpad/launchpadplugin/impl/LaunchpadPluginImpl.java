package de.tobias.playpad.launchpadplugin.impl;

import de.tobias.playpad.launchpadplugin.LaunchpadPlugin;
import de.tobias.playpad.launchpadplugin.midi.device.mk2.LaunchPadMK2;
import de.tobias.playpad.launchpadplugin.midi.device.s.LaunchPadS;
import de.tobias.playpad.midi.device.DeviceRegistry;
import de.tobias.playpad.plugin.Module;
import de.tobias.updater.client.Updatable;
import de.tobias.utils.util.Localization;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.events.Shutdown;

import java.util.ResourceBundle;

@PluginImplementation
public class LaunchpadPluginImpl implements LaunchpadPlugin {

	private static final String NAME = "LaunchPadPlugin";
	private static final String IDENTIFIER = "de.tobias.playwall.plugin.launchpad";

	private static ResourceBundle bundle;
	private LaunchPadPluginUpdater updater;
	private Module module;

	@PluginLoaded
	public void onLoaded(LaunchpadPlugin plugin) {
		bundle = Localization.loadBundle("de/tobias/playpad/launchpadplugin/assets/launchpad", LaunchpadPluginImpl.class.getClassLoader());
		updater = new LaunchPadPluginUpdater();
		module = new Module(NAME, IDENTIFIER);

		DeviceRegistry deviceFactory = DeviceRegistry.getFactoryInstance();
		deviceFactory.registerDevice(LaunchPadMK2.NAME, LaunchPadMK2.class);
		deviceFactory.registerDevice(LaunchPadS.NAME, LaunchPadS.class);

		System.out.println("Enable LaunchPad Plugin");
	}

	@Shutdown
	public void onShutdown() {
		System.out.println("Disable LaunchPad Plugin");
	}

	public static ResourceBundle getBundle() {
		return bundle;
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
