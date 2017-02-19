package de.tobias.playpad.server.sync.listener;

import de.tobias.playpad.server.sync.listener.downstream.ServerListener;

/**
 * Created by tobias on 19.02.17.
 */
public class ListenerUtils {
	public static boolean isNewValueComingFromServer() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (StackTraceElement element : stackTrace) {
			try {
				Class<?> clazz = Class.forName(element.getClassName());
				if (clazz == ServerListener.class) {
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}
