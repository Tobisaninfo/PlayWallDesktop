package de.tobias.playpad.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.tobias.playpad.plugin.ModernPlugin;
import de.tobias.updater.client.UpdateChannel;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by tobias on 10.02.17.
 */
public class ServerImpl implements Server {

	private String server;

	ServerImpl(String server) {
		this.server = server;
	}

	@Override
	public List<ModernPlugin> getPlugins() throws IOException {
		URL url = new URL(server + "/plugins");
		Reader reader = new InputStreamReader(url.openStream());
		Type listType = new TypeToken<List<ModernPlugin>>() {
		}.getType();

		Gson gson = new Gson();
		return gson.fromJson(reader, listType);
	}

	@Override
	public void loadPlugin(ModernPlugin plugin, UpdateChannel channel) throws IOException {
		URL url = new URL(server + "/" + channel + plugin.getPath());
		Path path = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, plugin.getFileName());
		Files.copy(url.openStream(), path);
	}
}
