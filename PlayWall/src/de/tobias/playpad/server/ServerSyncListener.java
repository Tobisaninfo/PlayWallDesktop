package de.tobias.playpad.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFrame;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.server.sync.command.CommandExecutor;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.conflict.Conflict;
import de.tobias.playpad.server.sync.conflict.ConflictSolver;
import de.tobias.playpad.server.sync.conflict.ConflictType;
import de.tobias.playpad.server.sync.conflict.Version;
import de.tobias.playpad.server.sync.listener.ServerListener;
import de.tobias.playpad.server.sync.listener.downstream.design.DesignAddListener;
import de.tobias.playpad.server.sync.listener.downstream.design.DesignUpdateListener;
import de.tobias.playpad.server.sync.listener.downstream.pad.*;
import de.tobias.playpad.server.sync.listener.downstream.page.PageAddListener;
import de.tobias.playpad.server.sync.listener.downstream.page.PageRemoveListener;
import de.tobias.playpad.server.sync.listener.downstream.page.PageUpdateListener;
import de.tobias.playpad.server.sync.listener.downstream.path.PathAddListener;
import de.tobias.playpad.server.sync.listener.downstream.path.PathRemoveListener;
import de.tobias.playpad.server.sync.listener.downstream.project.ProjectAddListener;
import de.tobias.playpad.server.sync.listener.downstream.project.ProjectRemoveListener;
import de.tobias.playpad.server.sync.listener.downstream.project.ProjectUpdateListener;
import de.tobias.playpad.viewcontroller.dialog.project.ProjectManagerDialog;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tobias on 19.02.17.
 */
public class ServerSyncListener extends WebSocketAdapter {

	private ObjectProperty<ConnectionState> connectionStateProperty;

	private Map<String, ServerListener> commands;

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
		commands.put(Commands.PAD_REMOVE, new PadRemoveListener());
		commands.put(Commands.PAD_MOVE, new PadMoveListener());

		commands.put(Commands.PATH_ADD, new PathAddListener());
		commands.put(Commands.PATH_REMOVE, new PathRemoveListener());

		commands.put(Commands.DESIGN_ADD, new DesignAddListener());
		commands.put(Commands.DESIGN_UPDATE, new DesignUpdateListener());

		connectionStateProperty = new SimpleObjectProperty<>(ConnectionState.CONNECTION_LOST);
	}

	@Override
	public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
		System.out.println("Connected");
		connectionStateProperty.set(ConnectionState.CONNECTED);


		// Handle Conflicts for all projects
		CommandExecutor commandExecutor = PlayPadPlugin.getCommandExecutorHandler().getCommandExecutor();
		ConflictSolver solver = commandExecutor.getConflictSolver();

		// Clear old conflicts
		commandExecutor.conflicts().clear();

		// Find new conflicts
		for (ProjectReference reference : ProjectReferenceManager.getProjects()) {
			ConflictType conflictType = solver.checkConflict(commandExecutor, reference);
			if (conflictType != ConflictType.NON) {
				List<Version> versions = solver.getVersions(reference);

				Conflict conflict = new Conflict(reference, conflictType, versions);
				commandExecutor.conflicts().add(conflict);
			}
		}
	}

	@Override
	public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
		System.out.println("Disconnected: " + clientCloseFrame.getCloseReason());
		connectionStateProperty.set(ConnectionState.CONNECTION_LOST);
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

	ObjectProperty<ConnectionState> connectionStateProperty() {
		return connectionStateProperty;
	}
}
