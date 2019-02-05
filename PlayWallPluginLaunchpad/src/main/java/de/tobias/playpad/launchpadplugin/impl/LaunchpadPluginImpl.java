package de.tobias.playpad.launchpadplugin.impl;

import de.thecodelabs.plugins.PluginArtifact;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.launchpadplugin.midi.mk2.LaunchPadMK2;
import de.tobias.playpad.launchpadplugin.midi.s.LaunchPadS;
import de.tobias.playpad.midi.device.DeviceRegistry;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.PlayPadPluginStub;

import java.util.ResourceBundle;

public class LaunchpadPluginImpl implements PlayPadPluginStub, PluginArtifact {

	private static final String NAME = "LaunchPadPlugin";
	private static final String IDENTIFIER = "de.tobias.playwall.plugin.launchpad";

	private static ResourceBundle bundle;

	private Module module;

	@Override
	public void startup() {
		bundle = Localization.loadBundle("lang/launchpad", LaunchpadPluginImpl.class.getClassLoader());
		module = new Module(NAME, IDENTIFIER);

		DeviceRegistry deviceFactory = DeviceRegistry.getFactoryInstance();
		deviceFactory.registerDevice(LaunchPadMK2.NAME, LaunchPadMK2.class);
		deviceFactory.registerDevice(LaunchPadS.NAME, LaunchPadS.class);

		System.out.println("Enable LaunchPad Plugin");
	}

	@Override
	public void shutdown() {
		System.out.println("Disable LaunchPad Plugin");
	}

	public static ResourceBundle getBundle() {
		return bundle;
	}

	@Override
	public Module getModule() {
		return module;
	}

}
