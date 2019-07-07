package de.tobias.playpad.launchpadplugin.impl;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.plugins.PluginArtifact;
import de.thecodelabs.plugins.PluginDescriptor;
import de.tobias.playpad.launchpadplugin.midi.mk2.LaunchPadMK2;
import de.tobias.playpad.launchpadplugin.midi.s.LaunchPadS;
import de.tobias.playpad.midi.device.DeviceRegistry;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.PlayPadPluginStub;

@SuppressWarnings("unused")
public class LaunchpadPluginImpl implements PlayPadPluginStub, PluginArtifact {

	private Module module;

	@Override
	public void startup(PluginDescriptor descriptor) {
		module = new Module(descriptor.getName(), descriptor.getArtifactId());

		DeviceRegistry deviceFactory = DeviceRegistry.getFactoryInstance();
		deviceFactory.registerDevice(LaunchPadMK2.NAME, LaunchPadMK2.class);
		deviceFactory.registerDevice(LaunchPadS.NAME, LaunchPadS.class);

		Logger.debug("Enable LaunchPad Plugin");
	}

	@Override
	public void shutdown() {
		Logger.debug("Disable LaunchPad Plugin");
	}

	@Override
	public Module getModule() {
		return module;
	}

}
