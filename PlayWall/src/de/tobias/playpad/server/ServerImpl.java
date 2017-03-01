package de.tobias.playpad.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.plugin.ModernPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectJsonReader;
import de.tobias.playpad.project.ProjectJsonWriter;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.command.design.DesignAddCommand;
import de.tobias.playpad.server.sync.command.design.DesignUpdateCommand;
import de.tobias.playpad.server.sync.command.pad.*;
import de.tobias.playpad.server.sync.command.page.PageAddCommand;
import de.tobias.playpad.server.sync.command.page.PageRemoveCommand;
import de.tobias.playpad.server.sync.command.page.PageUpdateCommand;
import de.tobias.playpad.server.sync.command.path.PathAddCommand;
import de.tobias.playpad.server.sync.command.path.PathRemoveCommand;
import de.tobias.playpad.server.sync.command.path.PathUpdateCommand;
import de.tobias.playpad.server.sync.command.project.ProjectAddCommand;
import de.tobias.playpad.server.sync.command.project.ProjectRemoveCommand;
import de.tobias.playpad.server.sync.command.project.ProjectUpdateCommand;
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
import java.util.*;

/**
 * Created by tobias on 10.02.17.
 */
public class ServerImpl implements Server, ChangeListener<ConnectionState> {

	private static final String OK = "OK";

	private String host;
	private WebSocket websocket;
	private ServerSyncListener syncListener;

	private Queue<String> dataQueue;

	ServerImpl(String host) {
		this.host = host;
		this.syncListener = new ServerSyncListener();
		this.syncListener.connectionStateProperty().addListener(this);

		dataQueue = new LinkedList<>();

		Path path = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, "Server.json");
		loadDataQueue(path);

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
		CommandManager.register(Commands.PATH_UPDATE, new PathUpdateCommand());
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

		// Save Data Queue
		Path path = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, "Server.json");
		saveDataQueue(path);
	}

	@Override
	public void push(String data) {
		if (ApplicationUtils.getApplication().isDebug()) {
			System.out.println("Send: " + data);
		}
		if (websocket.isOpen()) {
			websocket.sendText(data);
		} else {
			dataQueue.add(data);
		}
	}

	@Override
	public void push(JsonElement json) {
		push(json.toString());
	}

	@Override
	public void changed(ObservableValue<? extends ConnectionState> observable, ConnectionState oldValue, ConnectionState newValue) {
		if (newValue == ConnectionState.CONNECTION_LOST) {
			Worker.runLater(() -> {
				boolean connected = false;
				int count = 0;
				while (!connected && count < 20) {
					count++;
					try {
						Thread.sleep(30 * 1000);
						websocket = websocket.recreate().connect();
						connected = true;
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

	private void loadDataQueue(Path path) {
		if (Files.exists(path)) {
			try {
				dataQueue.addAll(Files.readAllLines(path));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveDataQueue(Path path) {
		try {
			Files.write(path, dataQueue, StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
