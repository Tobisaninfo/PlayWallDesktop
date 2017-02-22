package de.tobias.playpad.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;
import de.tobias.playpad.server.sync.Commands;
import de.tobias.playpad.server.sync.listener.downstream.pad.PadAddListener;
import de.tobias.playpad.server.sync.listener.downstream.pad.PadClearListener;
import de.tobias.playpad.server.sync.listener.downstream.pad.PadUpdateListener;
import de.tobias.playpad.server.sync.listener.downstream.page.PageAddListener;
import de.tobias.playpad.server.sync.listener.downstream.page.PageRemoveListener;
import de.tobias.playpad.server.sync.listener.downstream.page.PageUpdateListener;
import de.tobias.playpad.server.sync.listener.downstream.path.PathAddListener;
import de.tobias.playpad.server.sync.listener.downstream.path.PathRemoveListener;
import de.tobias.playpad.server.sync.listener.downstream.path.PathUpdateListener;
import de.tobias.playpad.server.sync.listener.downstream.project.ProjectAddListener;
import de.tobias.playpad.server.sync.listener.downstream.project.ProjectRemoveListener;
import de.tobias.playpad.server.sync.listener.downstream.project.ProjectUpdateListener;

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

		commands.put(Commands.PAGE_ADD, new PageAddListener());
		commands.put(Commands.PAGE_UPDATE, new PageUpdateListener());
		commands.put(Commands.PAGE_REMOVE, new PageRemoveListener());

		commands.put(Commands.PAD_ADD, new PadAddListener());
		commands.put(Commands.PAD_UPDATE, new PadUpdateListener());
		commands.put(Commands.PAD_CLEAR, new PadClearListener());

		commands.put(Commands.PATH_ADD, new PathAddListener());
		commands.put(Commands.PATH_UPDATE, new PathUpdateListener());
		commands.put(Commands.PATH_REMOVE, new PathRemoveListener());
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
