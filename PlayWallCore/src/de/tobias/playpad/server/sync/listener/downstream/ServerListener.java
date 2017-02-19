package de.tobias.playpad.server.sync.listener.downstream;

import com.google.gson.JsonElement;

/**
 * Created by tobias on 19.02.17.
 */
public interface ServerListener {

	void listen(JsonElement element);
}
