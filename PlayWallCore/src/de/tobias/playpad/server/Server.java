package de.tobias.playpad.server;

import com.google.gson.JsonElement;
import com.neovisionaries.ws.client.WebSocketException;
import de.tobias.playpad.plugin.ModernPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.updater.client.UpdateChannel;

import java.io.IOException;
import java.util.List;

/**
 * Created by tobias on 10.02.17.
 */
public interface Server {

	/**
	 * Load the list of available plugins from the server
	 *
	 * @return
	 * @throws IOException
	 */
	List<ModernPlugin> getPlugins() throws IOException;

	/**
	 * Load a plugin file from the server
	 *
	 * @param plugin  plugin file
	 * @param channel update channel
	 * @throws IOException network error
	 */
	void loadPlugin(ModernPlugin plugin, UpdateChannel channel) throws IOException;

	/**
	 * Create a session on the server side and return the private key for the communication.
	 *
	 * @param username username
	 * @param password password
	 * @return session key
	 */
	String getSession(String username, String password) throws IOException, LoginException;

	/**
	 * Get a list of the synced projects.
	 *
	 * @return synced projects
	 * @throws IOException notwork error
	 */
	List<ProjectReference> getSyncedProjects() throws IOException, LoginException;

	/**
	 * Load the project from the server.
	 *
	 * @param ref project reference
	 * @return project
	 * @throws IOException network error
	 */
	Project getProject(ProjectReference ref) throws IOException;

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
	 *
	 * @param json data
	 */
	void push(JsonElement json);
}
