package de.tobias.playpad.plugin.playout;

import de.thecodelabs.logger.LogLevel;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.plugins.PluginArtifact;
import de.thecodelabs.plugins.PluginDescriptor;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.PlayPadPluginStub;
import de.tobias.playpad.plugin.playout.log.LogSeasons;
import de.tobias.playpad.plugin.playout.log.listener.PadPlayLogListener;
import de.tobias.playpad.plugin.playout.viewcontroller.MainViewControllerListener;
import de.tobias.playpad.plugin.playout.viewcontroller.PlayoutLogStatusIconListener;
import de.tobias.playpad.settings.keys.Key;
import de.tobias.playpad.settings.keys.KeyCollectionEntry;
import de.tobias.playpad.settings.keys.KeyConflictException;

@SuppressWarnings("unused")
public class PlayoutLogPlugin implements PlayPadPluginStub, PluginArtifact {

	private Module module;

	public static final String KEY_COLLECTION_PLAYOUT = "playoutlog";

	@Override
	public void startup(PluginDescriptor descriptor) {
		Localization.addResourceBundle("lang/playoutlog", getClass().getClassLoader());

		module = new Module(descriptor.getName(), descriptor.getArtifactId());

		// Register Key Mapping
		KeyCollectionEntry keyCollectionEntry = new KeyCollectionEntry("Playout Log", new Key(KEY_COLLECTION_PLAYOUT));
		try {
			PlayPadPlugin.getInstance().getGlobalSettings().getKeyCollection().register(keyCollectionEntry);
		} catch (KeyConflictException e) {
			Logger.error(e);
		}

		PlayoutLogStatusIconListener playoutLogStatusIconListener = new PlayoutLogStatusIconListener();

		PlayPadPlugin.getInstance().addMainViewListener(new MainViewControllerListener());
		PlayPadPlugin.getInstance().addMainViewListener(playoutLogStatusIconListener);

		PlayPadPlugin.getInstance().addGlobalListener(new ProjectListener());
		PlayPadPlugin.getInstance().addPadListener(new PadPlayLogListener());

		LogSeasons.addListener(playoutLogStatusIconListener);

		PlayOutLogInitializer.init();

		Logger.debug("Enable Playout Log Plugin");
	}

	@Override
	public void shutdown() {
		Logger.debug("Disable Playout Log Plugin");

		try {
			LogSeasons.getStorageHandler().close();
		} catch (Exception e) {
			Logger.log(LogLevel.ERROR, "Cannot close LogSeasonStorageHandler (" + e.getLocalizedMessage() + ")");
		}
	}

	@Override
	public Module getModule() {
		return module;
	}
}
