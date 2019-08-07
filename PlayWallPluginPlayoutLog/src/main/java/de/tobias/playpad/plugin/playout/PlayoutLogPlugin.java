package de.tobias.playpad.plugin.playout;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.plugins.PluginArtifact;
import de.thecodelabs.plugins.PluginDescriptor;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.PlayPadPluginStub;
import de.tobias.playpad.plugin.playout.viewcontroller.MainViewControllerListener;

@SuppressWarnings("unused")
public class PlayoutLogPlugin implements PlayPadPluginStub, PluginArtifact {

	private Module module;

	@Override
	public void startup(PluginDescriptor descriptor) {
		Localization.addResourceBundle("lang/playoutlog", getClass().getClassLoader());

		module = new Module(descriptor.getName(), descriptor.getArtifactId());
		PlayPadPlugin.getInstance().addMainViewListener(new MainViewControllerListener());
		PlayOutLogInitializer.init();

		Logger.debug("Enable Playout Log Plugin");
	}

	@Override
	public void shutdown() {
		Logger.debug("Disable Playout Log Plugin");
	}

	@Override
	public Module getModule() {
		return module;
	}
}
