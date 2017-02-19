package de.tobias.playpad.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neovisionaries.ws.client.WebSocketException;
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

	/**
	 * Connect to sync server with key.
	 *
	 * @param key auth key
	 */
	void connect(String key) throws IOException, WebSocketException;

	/**
	 * Disconnect from sync server.
	 */
	void disconnect();

	/**
	 * Send data upstream to server.
	 *
	 * @param data data
	 */
	void push(String data);

	/**
	 * Send datat upstream to server.
	 * @param json data
	 */
	void push(JsonElement json);
}
