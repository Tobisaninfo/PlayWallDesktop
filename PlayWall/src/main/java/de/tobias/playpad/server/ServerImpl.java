package de.tobias.playpad.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.versionizer.service.UpdateService;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.plugin.ModernPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectJsonReader;
import de.tobias.playpad.project.ProjectJsonWriter;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.CommandStore;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.command.pad.*;
import de.tobias.playpad.server.sync.command.pad.settings.PadSettingsAddCommand;
import de.tobias.playpad.server.sync.command.pad.settings.PadSettingsUpdateCommand;
import de.tobias.playpad.server.sync.command.pad.settings.design.DesignAddCommand;
import de.tobias.playpad.server.sync.command.pad.settings.design.DesignUpdateCommand;
import de.tobias.playpad.server.sync.command.page.PageAddCommand;
import de.tobias.playpad.server.sync.command.page.PageRemoveCommand;
import de.tobias.playpad.server.sync.command.page.PageUpdateCommand;
import de.tobias.playpad.server.sync.command.path.PathAddCommand;
import de.tobias.playpad.server.sync.command.path.PathRemoveCommand;
import de.tobias.playpad.server.sync.command.project.ProjectAddCommand;
import de.tobias.playpad.server.sync.command.project.ProjectRemoveCommand;
import de.tobias.playpad.server.sync.command.project.ProjectUpdateCommand;
import de.tobias.playpad.server.sync.conflict.Version;
import io.github.openunirest.http.HttpResponse;
import io.github.openunirest.http.JsonNode;
import io.github.openunirest.http.Unirest;
import io.github.openunirest.http.exceptions.UnirestException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by tobias on 10.02.17.
 */
public class ServerImpl implements Server, ChangeListener<ConnectionState> {

	private static final String OK = "OK";
	private static final String CACHE_FOLDER = "Cache";
	private static final String PROTOCOL = "https";
	private static final String WS_PROTOCOL = "wss";

	private final String host;
	private WebSocket websocket;
	private final ServerSyncListener syncListener;

	ServerImpl(String host) {
		this.host = host;
		this.syncListener = new ServerSyncListener();
		this.syncListener.connectionStateProperty().addListener(this);

		try {
			loadStoredFiles();
		} catch (IOException e) {
			Logger.error(e);
		}
		registerCommands();
	}

	private void registerCommands() {
		CommandManager.register(Commands.PROJECT_ADD, new ProjectAddCommand());
		CommandManager.register(Commands.PROJECT_UPDATE, new ProjectUpdateCommand());
		CommandManager.register(Commands.PROJECT_REMOVE, new ProjectRemoveCommand());

		CommandManager.register(Commands.PAGE_ADD, new PageAddCommand());
		CommandManager.register(Commands.PAGE_UPDATE, new PageUpdateCommand());
		CommandManager.register(Commands.PAGE_REMOVE, new PageRemoveCommand());

		CommandManager.register(Commands.PAD_ADD, new PadAddCommand());
		CommandManager.register(Commands.PAD_UPDATE, new PadUpdateCommand());
		CommandManager.register(Commands.PAD_CLEAR, new PadClearCommand());
		CommandManager.register(Commands.PAD_REMOVE, new PadRemoveCommand());
		CommandManager.register(Commands.PAD_MOVE, new PadMoveCommand());

		CommandManager.register(Commands.PATH_ADD, new PathAddCommand());
		CommandManager.register(Commands.PATH_REMOVE, new PathRemoveCommand());

		CommandManager.register(Commands.DESIGN_ADD, new DesignAddCommand());
		CommandManager.register(Commands.DESIGN_UPDATE, new DesignUpdateCommand());

		CommandManager.register(Commands.PAD_SETTINGS_ADD, new PadSettingsAddCommand());
		CommandManager.register(Commands.PAD_SETTINGS_UPDATE, new PadSettingsUpdateCommand());
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public List<ModernPlugin> getPlugins() throws IOException {
		URL url = new URL(PROTOCOL + "://" + host + "/plugins");
		Reader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
		Type listType = new TypeToken<List<ModernPlugin>>() {
		}.getType();

		Gson gson = new Gson();
		return gson.fromJson(reader, listType);
	}

	@Override
	public ModernPlugin getPlugin(String id) throws IOException {
		URL url = new URL(PROTOCOL + "://" + host + "/plugins/" + id);
		Reader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
		Gson gson = new Gson();
		return gson.fromJson(reader, ModernPlugin.class);
	}

	@Override
	public void loadPlugin(ModernPlugin plugin, UpdateService.RepositoryType channel) throws IOException {
		Path destination = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, plugin.getFileName());

		String url = PROTOCOL + "://" + host + "/plugins/raw/" + plugin.getId();
		Logger.debug("Load server resource: {0}", destination);
		try {
			HttpResponse<InputStream> response = Unirest.get(url).asBinary();
			Files.copy(response.getBody(), destination, StandardCopyOption.REPLACE_EXISTING);
		} catch (UnirestException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public String getSession(String username, String password) throws IOException, LoginException {
		String url = PROTOCOL + "://" + host + "/sessions";
		try {
			HttpResponse<JsonNode> response = Unirest.post(url)
					.queryString("username", username)
					.queryString("password", password)
					.asJson();
			JSONObject object = response.getBody().getObject();

			// Account Error
			if (!object.getString("status").equals(OK)) {
				throw new LoginException(object.getString("message"));
			}
			// Session Key
			return object.getString("key");
		} catch (UnirestException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public void logout(String username, String password, String key) throws IOException {
		String url = PROTOCOL + "://" + host + "/sessions";
		try {
			Unirest.post(url)
					.queryString("username", username)
					.queryString("password", password)
					.queryString("session", key)
					.asJson();
		} catch (UnirestException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public List<ProjectReference> getSyncedProjects() throws IOException, LoginException {
		String url = PROTOCOL + "://" + host + "/projects";
		try {
			Session session = PlayPadMain.getProgramInstance().getSession();
			HttpResponse<JsonNode> request = Unirest.get(url)
					.queryString("session", session.getKey())
					.asJson();
			JsonNode body = request.getBody();

			if (body.isArray()) {
				JSONArray array = body.getArray();

				List<ProjectReference> projects = new ArrayList<>();
				for (int i = 0; i < array.length(); i++) {
					JSONObject object = array.getJSONObject(i);
					UUID uuid = UUID.fromString(object.getString("uuid"));
					String name = object.getString("name");

					ProjectReference ref = new ProjectReference(uuid, name, true);
					projects.add(ref);
				}
				return projects;
			} else {
				throw new LoginException(body.getObject().getString("message"));
			}
		} catch (UnirestException e) {
			throw new IOException(e.getMessage());
		} catch (SessionNotExistsException ignored) {
			return new ArrayList<>();
		}
	}

	@Override
	public Project getProject(ProjectReference ref) throws IOException {
		String url = PROTOCOL + "://" + host + "/projects/" + ref.getUuid();
		Session session = PlayPadMain.getProgramInstance().getSession();
		try {
			HttpResponse<JsonNode> response = Unirest.get(url)
					.queryString("session", session.getKey())
					.asJson();

			JSONObject object = response.getBody().getObject();
			ProjectJsonReader reader = new ProjectJsonReader(object);
			return reader.read(ref);
		} catch (UnirestException e) {
			throw new IOException(e.getMessage());
		} catch (SessionNotExistsException ignored) {
			return null;
		}
	}

	@Override
	public void postProject(Project project) throws IOException {
		String url = PROTOCOL + "://" + host + "/projects";
		Session session = PlayPadMain.getProgramInstance().getSession();
		try {
			ProjectJsonWriter writer = new ProjectJsonWriter();

			String value = writer.write(project).toString();

			Unirest.post(url)
					.queryString("session", session.getKey())
					.queryString("project", value)
					.asJson();
		} catch (UnirestException e) {
			throw new IOException(e.getMessage(), e);
		} catch (SessionNotExistsException ignored) {
		}
	}

	@Override
	public Version getLastServerModification(ProjectReference ref) throws IOException {
		String url = PROTOCOL + "://" + host + "/projects/modification/" + ref.getUuid();
		Session session = PlayPadMain.getProgramInstance().getSession();
		try {
			HttpResponse<JsonNode> response = Unirest.get(url)
					.queryString("session", session.getKey())
					.asJson();

			JSONObject object = response.getBody().getObject();
			String remoteSession = object.getString("session");
			long time = object.getLong("time");
			return new Version(time, remoteSession, false);
		} catch (UnirestException e) {
			throw new IOException(e.getMessage());
		} catch (SessionNotExistsException ignored) {
			return null;
		}
	}

	@Override
	public void connect(String key) {
		try {
			WebSocketFactory webSocketFactory = new WebSocketFactory();
			webSocketFactory.setConnectionTimeout(5000);
			if (PlayPadMain.sslContext != null) {
				webSocketFactory.setSSLContext(PlayPadMain.sslContext);
			}
			websocket = webSocketFactory.createSocket(WS_PROTOCOL + "://" + host + "/project");
			websocket.addHeader("key", key);

			websocket.addListener(syncListener);
			websocket.connect();
		} catch (WebSocketException | IOException e) {
			Logger.error("Failed to connect to server: " + e.getMessage());
		}
	}

	@Override
	public void disconnect() {
		Logger.info("Disconnect from Server");
		websocket.disconnect();

		try {
			saveStoredCommands();
		} catch (IOException e) {
			Logger.error(e);
		}
	}

	@Override
	public boolean push(String data) {
		if (websocket.isOpen()) {
			if (ApplicationUtils.getApplication().isDebug()) {
				Logger.debug("Send: " + data);
			}
			// Send to Server
			websocket.sendText(data);
			return true;
		}
		return false;
	}

	@Override
	public boolean push(JsonElement json) {
		return push(json.toString());
	}

	// Reconnect
	@Override
	public void changed(ObservableValue<? extends ConnectionState> observable, ConnectionState oldValue, ConnectionState newValue) {
		if (newValue == ConnectionState.CONNECTION_REFUSED) {
			Worker.runLater(this::reconnect);
		}
	}

	@Override
	public ConnectionState getConnectionState() {
		return syncListener.connectionStateProperty().get();
	}

	@Override
	public ObjectProperty<ConnectionState> connectionStateProperty() {
		return syncListener.connectionStateProperty();
	}

	private void loadStoredFiles() throws IOException {
		Path path = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, CACHE_FOLDER);
		if (Files.exists(path)) {
			try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
				for (Path file : directoryStream) {
					loadStoredFile(file);
				}
			}
		}
	}

	private void loadStoredFile(Path path) throws IOException {
		final List<String> lines = Files.readAllLines(path);
		final List<JsonObject> commands = lines.stream()
				.map(line -> (JsonObject) JsonParser.parseString(line))
				.collect(Collectors.toList());

		CommandStore executor = (CommandStore) PlayPadPlugin.getCommandExecutorHandler().getCommandExecutor();
		executor.setStoredCommands(path.getFileName().toString(), commands);
	}

	private void saveStoredCommands() throws IOException {
		Path folder = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, CACHE_FOLDER);

		if (Files.notExists(folder)) {
			Files.createDirectories(folder);
		}

		CommandStore executor = (CommandStore) PlayPadPlugin.getCommandExecutorHandler().getCommandExecutor();
		Map<UUID, List<JsonObject>> storedCommands = executor.getStoredCommands();
		for (Map.Entry<UUID, List<JsonObject>> entry : storedCommands.entrySet()) {
			Path file = folder.resolve(entry.getKey().toString());
			List<String> lines = entry.getValue().stream().map(JsonElement::toString).collect(Collectors.toList());
			Files.write(file, lines, StandardOpenOption.CREATE);
		}
	}

	private void reconnect() {
		boolean connected = false;
		int count = 0;
		while (!connected && count < 20) {
			count++;
			try {
				websocket = websocket.recreate().connect();
				connected = true;
				Thread.sleep(30 * 1000L);
			} catch (InterruptedException e) {
				break;
			} catch (WebSocketException | IOException ignored) {
			}
		}
	}
}
