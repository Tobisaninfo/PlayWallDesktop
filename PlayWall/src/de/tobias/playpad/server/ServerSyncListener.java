package de.tobias.playpad.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.listener.downstream.ProjectAddListener;
import de.tobias.playpad.server.sync.listener.downstream.ProjectRemoveListener;
import de.tobias.playpad.server.sync.listener.downstream.ProjectUpdateListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tobias on 19.02.17.
 */
public class ServerSyncListener extends WebSocketAdapter {

	private Map<String, de.tobias.playpad.server.sync.listener.downstream.ServerListener> commands;

	ServerSyncListener() {
		commands = new HashMap<>();
		commands.put(Commands.PROJECT_ADD, new ProjectAddListener());
		commands.put(Commands.PROJECT_UPDATE, new ProjectUpdateListener());
		commands.put(Commands.PROJECT_REMOVE, new ProjectRemoveListener());
	}

	@Override
	public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
		System.out.println("Connected");
	}

	@Override
	public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
		System.out.println("Disconnected: " + clientCloseFrame.getCloseReason());
	}

	@Override
	public void onTextMessage(WebSocket websocket, String text) throws Exception {
		JsonParser parser = new JsonParser();
		JsonElement element = parser.parse(text);
		if (element instanceof JsonObject) {
			JsonObject json = (JsonObject) element;
			String cmd = json.get("cmd").getAsString();
			commands.get(cmd).listen(json);
		}
	}
}
