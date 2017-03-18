package de.tobias.playpad.server;

import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by tobias on 21.02.17.
 */
public class Session {

	private static final String SESSION_KEY = "session.key";

	private String key;

	public Session(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public static Session load() {
		Path path = ApplicationUtils.getApplication().getPath(PathType.STORE, SESSION_KEY);
		try {
			byte[] key = Files.readAllBytes(path);
			return new Session(new String(key));
		} catch (IOException ignored) {
		}
		return null;
	}

	public void save() {
		Path path = ApplicationUtils.getApplication().getPath(PathType.STORE, SESSION_KEY);
		try {
			if (Files.notExists(path)) {
				Files.createDirectories(path.getParent());
				Files.createFile(path);
			}
			Files.write(path, key.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
