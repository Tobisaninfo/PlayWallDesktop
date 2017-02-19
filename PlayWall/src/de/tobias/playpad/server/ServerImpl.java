package de.tobias.playpad.server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.neovisionaries.ws.client.*;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.plugin.ModernPlugin;
import de.tobias.playpad.server.sync.listener.downstream.ProjectListener;
import de.tobias.updater.client.UpdateChannel;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by tobias on 10.02.17.
 */
public class ServerImpl implements Server {

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
	public void connect(String key) throws IOException, WebSocketException {
		WebSocketFactory webSocketFactory = new WebSocketFactory();
		if (PlayPadMain.sslContext != null) {
			webSocketFactory.setSSLContext(PlayPadMain.sslContext);
		}
		websocket = webSocketFactory.createSocket("wss://" + host + "/project");
		websocket.addHeader("key", key);
		websocket.addListener(new WebSocketAdapter(){
			@Override
			public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
				System.err.println("Disconnected: " + clientCloseFrame);
			}

			@Override
			public void onTextMessage(WebSocket websocket, String text) throws Exception {
				JsonParser parser = new JsonParser();
				JsonElement element = parser.parse(text);
				new ProjectListener().listen(element);
			}
		});
		websocket.connect();
	}

	@Override
	public void disconnect() {
		websocket.disconnect();
	}

	@Override
	public void push(String data) {
		websocket.sendText(data);
	}

	@Override
	public void push(JsonElement json) {
		push(json.toString());
	}
}
