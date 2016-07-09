package de.tobias.playpad.launchpadplugin.impl;

import java.util.ResourceBundle;

import de.tobias.playpad.launchpadplugin.LaunchpadPlugin;
import de.tobias.playpad.launchpadplugin.midi.device.mk2.LaunchPadMK2;
import de.tobias.playpad.launchpadplugin.midi.device.s.LaunchPadS;
import de.tobias.playpad.midi.device.DeviceRegistry;
import de.tobias.playpad.update.UpdateRegistery;
import de.tobias.utils.util.Localization;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.events.Shutdown;

@PluginImplementation
public class LaunchpadPluginImpl implements LaunchpadPlugin {

	private static ResourceBundle bundle;

	@PluginLoaded
	public void onLoaded(LaunchpadPlugin plugin) {
		bundle = Localization.loadBundle("de/tobias/playpad/launchpadplugin/assets/launchpad", LaunchpadPluginImpl.class.getClassLoader());

		UpdateRegistery.registerUpdateable(new LaunchPadPluginUpdater());

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

}
