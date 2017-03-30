package de.tobias.playpad.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
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
import de.tobias.playpad.server.sync.command.design.DesignAddCommand;
import de.tobias.playpad.server.sync.command.design.DesignUpdateCommand;
import de.tobias.playpad.server.sync.command.pad.*;
import de.tobias.playpad.server.sync.command.page.PageAddCommand;
import de.tobias.playpad.server.sync.command.page.PageRemoveCommand;
import de.tobias.playpad.server.sync.command.page.PageUpdateCommand;
import de.tobias.playpad.server.sync.command.path.PathAddCommand;
import de.tobias.playpad.server.sync.command.path.PathRemoveCommand;
import de.tobias.playpad.server.sync.command.project.ProjectAddCommand;
import de.tobias.playpad.server.sync.command.project.ProjectRemoveCommand;
import de.tobias.playpad.server.sync.command.project.ProjectUpdateCommand;
import de.tobias.playpad.server.sync.conflict.Version;
import de.tobias.updater.client.UpdateChannel;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.Worker;
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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

	private String host;
	private WebSocket websocket;
	private ServerSyncListener syncListener;

	ServerImpl(String host) {
		this.host = host;
		this.syncListener = new ServerSyncListener();
		this.syncListener.connectionStateProperty().addListener(this);

		try {
			loadStoredFiles();
		} catch (IOException e) {
			e.printStackTrace(); // TODO Error Handling
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
	}

	@Override
	public List<ModernPlugin> getPlugins() throws IOException {
		URL url = new URL("https://" + host + "/plugins");
		Reader reader = new InputStreamReader(url.openStream(), Charset.forName("UTF-8"));
		Type listType = new TypeToken<List<ModernPlugin>>() {
		}.getType();

		Gson gson = new Gson();
		return gson.fromJson(reader, listType);
	}

	@Override
	public void loadPlugin(ModernPlugin plugin, UpdateChannel channel) throws IOException {
		String url = "https://" + host + "/" + channel + plugin.getPath();
		try {
			HttpResponse<InputStream> response = Unirest.get(url).asBinary();
			Path path = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, plugin.getFileName());
			Files.copy(response.getBody(), path);
		} catch (UnirestException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public String getSession(String username, String password) throws IOException, LoginException {
		String url = "https://" + host + "/sessions";
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
	public List<ProjectReference> getSyncedProjects() throws IOException, LoginException {
		String url = "https://" + host + "/projects";
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

					ProjectReference ref = new ProjectReference(uuid, name);
					projects.add(ref);
				}
				return projects;
			} else {
				throw new LoginException(body.getObject().getString("message"));
			}
		} catch (UnirestException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	public Project getProject(ProjectReference ref) throws IOException {
		String url = "https://" + host + "/projects/" + ref.getUuid();
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
		}
	}

	@Override
	public void postProject(Project project) throws IOException {
		String url = "https://" + host + "/projects";
		Session session = PlayPadMain.getProgramInstance().getSession();
		try {
			ProjectJsonWriter writer = new ProjectJsonWriter();

			String value = writer.write(project).toString();
			System.out.println(value);

			Unirest.post(url)
					.queryString("session", session.getKey())
					.queryString("project", value)
					.asJson();
		} catch (UnirestException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	@Override
	public Version getLastServerModification(ProjectReference ref) throws IOException {
		String url = "https://" + host + "/projects/modification/" + ref.getUuid();
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
		}
	}

	@Override
	public void connect(String key) {
		try {
			WebSocketFactory webSocketFactory = new WebSocketFactory();
			if (PlayPadMain.sslContext != null) {
				webSocketFactory.setSSLContext(PlayPadMain.sslContext);
			}
			websocket = webSocketFactory.createSocket("wss://" + host + "/project");
			websocket.addHeader("key", key);

			websocket.addListener(syncListener);
			websocket.connect();
		} catch (WebSocketException | IOException e) {
			System.err.println("Failed to connect to server: " + e.getMessage());
		}
	}

	@Override
	public void disconnect() {
		System.out.println("Disconnect from Server");
		websocket.disconnect();

		try {
			saveStoredCommands();
		} catch (IOException e) {
			e.printStackTrace(); // TODO Error Handling
		}
	}

	@Override
	public boolean push(String data) {
		if (websocket.isOpen()) {
			if (ApplicationUtils.getApplication().isDebug()) {
				System.out.println("Send: " + data);
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
		if (newValue == ConnectionState.CONNECTION_LOST) {
			Worker.runLater(() -> {
				boolean connected = false;
				int count = 0;
				while (!connected && count < 20) {
					count++;
					try {
						websocket = websocket.recreate().connect();
						connected = true;
						Thread.sleep(30 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					} catch (WebSocketException | IOException ignored) {
					}
				}
			});
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
			for (Path file : Files.newDirectoryStream(path)) {
				loadStoredFile(file);
			}
		}
	}

	private void loadStoredFile(Path path) throws IOException {
		List<String> lines = Files.readAllLines(path);

		JsonParser parser = new JsonParser();
		List<JsonObject> commands = lines.stream().map(line -> (JsonObject) parser.parse(line)).collect(Collectors.toList());

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
		for (UUID key : storedCommands.keySet()) {
			Path file = folder.resolve(key.toString());
			List<String> lines = storedCommands.get(key).stream().map(JsonElement::toString).collect(Collectors.toList());
			Files.write(file, lines, StandardOpenOption.CREATE);
		}
	}
}
