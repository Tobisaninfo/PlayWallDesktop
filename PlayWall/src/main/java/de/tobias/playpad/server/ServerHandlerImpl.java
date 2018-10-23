package de.tobias.playpad.server;

import de.thecodelabs.utils.application.ApplicationUtils;
import de.tobias.playpad.AppUserInfoStrings;
import de.tobias.playpad.PlayPadImpl;
import de.tobias.playpad.PlayPadMain;
import javafx.application.Application;

import java.util.Map;

/**
 * Created by tobias on 10.02.17.
 */
public class ServerHandlerImpl implements ServerHandler {

	private static final String SERVER_PARAM = "server";

	private Server server;

	public Server getServer() {
		if (server == null) {
			String url = ApplicationUtils.getApplication().getInfo().getUserInfo().get(AppUserInfoStrings.SERVER).toString();

			PlayPadImpl impl = PlayPadMain.getProgramInstance();
			Application.Parameters parameters = impl.getParameters();

			Map<String, String> named = parameters.getNamed();
			if (named.containsKey(SERVER_PARAM)) {
				url = named.get(SERVER_PARAM);
			}

			server = new ServerImpl(url);
		}
		return server;
	}
}
