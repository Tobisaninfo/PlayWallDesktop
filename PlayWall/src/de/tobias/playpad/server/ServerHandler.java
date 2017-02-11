package de.tobias.playpad.server;

import de.tobias.playpad.AppUserInfoStrings;
import de.tobias.utils.application.ApplicationUtils;

/**
 * Created by tobias on 10.02.17.
 */
public class ServerHandler {

	private static Server server;

	public static Server getServer() {
		if (server == null) {
			String url = ApplicationUtils.getApplication().getInfo().getUserInfo().get(AppUserInfoStrings.SERVER).toString();
			server = new ServerImpl(url);
		}
		return server;
	}
}
