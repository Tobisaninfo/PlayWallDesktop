package de.tobias.playpad.server;

import com.google.gson.*;
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
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.updater.client.UpdateChannel;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by tobias on 10.02.17.
 */
public class ServerImpl implements Server {

	private static final String OK = "OK";

	private String host;
	private WebSocket websocket;

	ServerImpl(String host) {
		this.host = host;
	}

	@Override
	public List<ModernPlugin> getPlugins() throws IOException {
		URL url = new URL("https://" + host + "/plugins");
		Reader reader = new InputStreamReader(url.openStream(), Charset.forName("UTF-8"));
		Type listType = new TypeToken<List<ModernPlugin>>() {}.getType();

		Gson gson = new Gson();
		return gson.fromJson(reader, listType);
	}

	@Override
	public void loadPlugin(ModernPlugin plugin, UpdateChannel channel) throws IOException {
		URL url = new URL("https://" + host + "/" + channel + plugin.getPath());
		Path path = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, plugin.getFileName());
		Files.copy(url.openStream(), path);
	}

	@Override
	public String getSession(String username, String password) throws IOException, LoginException {
		String url = "https://" + host + "/sessions";
		try {
			HttpResponse<JsonNode> response = Unirest.post(url)
					.queryString("username", username)
					.queryString("password", password).asJson();

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
	public List<ProjectReference> getSyncedProjects() throws IOException {
		URL url = new URL("https://" + host + "/projects?session=3pRogQ63Bd1YTXNOBNM3uyujDv2EPjaIZwXcxT9TzHHGm9TKNIDEBqSlnWo0e25HEtiOvzR4H2nKx7uLvs0MM1z7g2XCvoiqxGo3");
		Reader reader = new InputStreamReader(url.openStream(), Charset.forName("UTF-8"));

		List<ProjectReference> projects = new ArrayList<>();
		JsonArray array = (JsonArray) new JsonParser().parse(reader);
		for (JsonElement element : array) {
			if (element instanceof JsonObject) {
				JsonObject json = (JsonObject) element;

				UUID uuid = UUID.fromString(json.get("uuid").getAsString());
				String name = json.get("name").getAsString();

				ProjectReference ref = new ProjectReference(uuid, name);
				projects.add(ref);
			}
		}
		return projects;
	}

	@Override
	public Project getProject(ProjectReference ref) throws IOException {
		return null;
	}

	@Override
	public void connect(String key) throws IOException, WebSocketException {
		WebSocketFactory webSocketFactory = new WebSocketFactory();
		if (PlayPadMain.sslContext != null) {
			webSocketFactory.setSSLContext(PlayPadMain.sslContext);
		}
		websocket = webSocketFactory.createSocket("wss://" + host + "/project");
		websocket.addHeader("key", key);
		websocket.addListener(new ServerSyncListener());
		websocket.connect();
	}

	@Override
	public void disconnect() {
		websocket.disconnect();
	}

	@Override
	public void push(String data) {
		if (ApplicationUtils.getApplication().isDebug()) {
			System.out.println("Send: " + data);
		}
		websocket.sendText(data);
	}

	@Override
	public void push(JsonElement json) {
		push(json.toString());
	}
}
