package de.tobias.playpad.server;

import com.google.gson.JsonElement;
import com.neovisionaries.ws.client.WebSocketException;
import de.thecodelabs.versionizer.service.UpdateService;
import de.tobias.playpad.plugin.ModernPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.sync.conflict.Version;
import javafx.beans.property.ObjectProperty;

import java.io.IOException;
import java.util.List;

/**
 * Interface for server communication.
 *
 * @author tobias
 * @version 6.2.0
 */
public interface Server {

	/**
	 * Get the host of the server.
	 *
	 * @return hostname
	 */
	String getHost();

	/**
	 * Load the list of available plugins from the server
	 *
	 * @return plugins
	 * @throws IOException network error
	 */
	List<ModernPlugin> getPlugins() throws IOException;

	/**
	 * Load plugin informtaion for a specific plugin.
	 *
	 * @param name plugin name
	 * @return plugin info
	 * @throws IOException network error
	 */
	ModernPlugin getPlugin(String name) throws IOException;

	/**
	 * Load a plugin file from the server
	 *
	 * @param plugin  plugin file
	 * @param channel update channel
	 * @throws IOException network error
	 */
	void loadPlugin(ModernPlugin plugin, UpdateService.RepositoryType channel) throws IOException;

	/**
	 * Create a session on the server side and return the private key for the communication.
	 *
	 * @param username username
	 * @param password password
	 * @return session key
	 */
	String getSession(String username, String password) throws IOException, LoginException;

	void logout(String username, String password, String key) throws IOException;

	/**
	 * Get a list of the synced projects.
	 *
	 * @return synced projects
	 * @throws IOException notwork error
	 */
	List<ProjectReference> getSyncedProjects() throws IOException, LoginException;

	/**
	 * Load the project from the server without local additional settings. To Load the full project you must use the {@link de.tobias.playpad.project.ProjectSyncSerializer}
	 *
	 * @param ref project reference
	 * @return project
	 * @throws IOException network error
	 */
	Project getProject(ProjectReference ref) throws IOException;

	/**
	 * Post a project to the server.
	 *
	 * @param project project
	 * @throws IOException network error
	 */
	void postProject(Project project) throws IOException;

	/**
	 * Get the last project modification.
	 *
	 * @param projectReference project reference
	 * @return last modification
	 */
	Version getLastServerModification(ProjectReference projectReference) throws IOException;

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
	 * @return success
	 */
	boolean push(String data);

	/**
	 * Send data upstream to server.
	 *
	 * @param json data
	 * @return success
	 */
	boolean push(JsonElement json);

	/**
	 * Get the connection state of the websocket connection.
	 *
	 * @return state
	 */
	ConnectionState getConnectionState();

	/**
	 * Get the connection state property of the websocket connection.
	 *
	 * @return state property
	 */
	ObjectProperty<ConnectionState> connectionStateProperty();
}
