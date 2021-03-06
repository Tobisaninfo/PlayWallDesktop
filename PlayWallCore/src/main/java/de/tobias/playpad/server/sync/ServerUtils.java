package de.tobias.playpad.server.sync;

import de.thecodelabs.logger.Logger;
import de.tobias.playpad.server.sync.listener.ServerListener;

/**
 * Created by tobias on 19.02.17.
 */
public class ServerUtils {

	private ServerUtils() {
	}

	public static boolean isNewValueComingFromServer() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : stackTrace) {
			try {
				if (element.getClassName().startsWith("de.")) {
					Class<?> clazz = Class.forName(element.getClassName());
					if (ServerListener.class.isAssignableFrom(clazz)) {
						return true;
					}
				}
			} catch (Exception e) {
				Logger.error(e);
			}
		}
		return false;
	}
}
