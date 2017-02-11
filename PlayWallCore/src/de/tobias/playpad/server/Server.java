package de.tobias.playpad.server;

import de.tobias.playpad.plugin.ModernPlugin;
import de.tobias.updater.client.UpdateChannel;

import java.io.IOException;
import java.util.List;

/**
 * Created by tobias on 10.02.17.
 */
public interface Server {

	List<ModernPlugin> getPlugins() throws IOException;

	void loadPlugin(ModernPlugin plugin, UpdateChannel channel) throws IOException;
}
