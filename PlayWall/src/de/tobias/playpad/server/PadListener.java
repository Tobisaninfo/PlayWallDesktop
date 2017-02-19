package de.tobias.playpad.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import javafx.application.Platform;

import java.util.UUID;

/**
 * Created by tobias on 13.02.17.
 */
@ServerListener
public class PadListener extends WebSocketAdapter {

	@Override
	public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
		System.err.println("Disconnected " + clientCloseFrame.getCloseReason());
	}

	@Override
	public void onTextMessage(WebSocket websocket, String text) throws Exception {
		JsonElement element = new JsonParser().parse(text);
		System.out.println(element);
		if (element instanceof JsonObject) {
			JsonObject object = (JsonObject) element;
			UUID index = UUID.fromString(object.get("id").getAsString());
			String value = object.get("value").getAsString();

			Project currentProject = PlayPadMain.getProgramInstance().getCurrentProject();
			Pad pad = currentProject.getPad(index);
			Platform.runLater(() -> pad.setName(value));
		}
	}
}
