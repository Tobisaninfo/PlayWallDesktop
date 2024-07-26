package de.tobias.playpad.launchpadplugin.impl;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.midi.midi.feedback.MidiFeedbackTranscriptionRegistry;
import de.thecodelabs.plugins.PluginDescriptor;
import de.thecodelabs.plugins.versionizer.PluginArtifact;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.launchpadplugin.midi.mk2.LaunchPadMK2;
import de.tobias.playpad.launchpadplugin.midi.mk3mini.LaunchPadMK3Mini;
import de.tobias.playpad.launchpadplugin.midi.s.LaunchPadS;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.PlayPadPluginStub;

@SuppressWarnings("unused")
public class LaunchpadPluginImpl implements PlayPadPluginStub, PluginArtifact {

	private Module module;

	@Override
	public void startup(PluginDescriptor descriptor) {
		module = new Module(descriptor.getName(), descriptor.getArtifactId());
		Localization.addResourceBundle("lang/l10n", LaunchpadPluginImpl.class.getClassLoader());

		final MidiFeedbackTranscriptionRegistry registry = MidiFeedbackTranscriptionRegistry.getInstance();
		registry.register(LaunchPadMK2.NAME, new LaunchPadMK2());
		registry.register(LaunchPadMK2.NATIVE_NAME, new LaunchPadMK2());
		registry.register(LaunchPadMK3Mini.NAME, new LaunchPadMK3Mini());
		registry.register(LaunchPadMK3Mini.NATIVE_NAME, new LaunchPadMK3Mini());
		registry.register(LaunchPadS.NAME, new LaunchPadS());
		registry.register(LaunchPadS.NATIVE_NAME, new LaunchPadS());

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
