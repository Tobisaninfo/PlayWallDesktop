package de.tobias.playpad.server;

import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by tobias on 21.02.17.
 */
public class Session {

	public static final Session EMPTY = new Session(null);

	private static final String SESSION_KEY = "session.txt";

	private String key;

	public Session(String key) {
		this.key = key;
	}

	public String getKey() throws SessionNotExistsException {
		if (key == null) {
			throw new SessionNotExistsException();
		}
		return key;
	}

	public static Session load() {
		Path path = getPath();
		try {
			byte[] key = Files.readAllBytes(path);
			return new Session(new String(key));
		} catch (IOException ignored) {
		}
		return EMPTY;
	}

	public void save() {
		if (key == null) {
			return;
		}

		Path path = getPath();
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

	public void delete() throws IOException {
		Path path = getPath();
		Files.delete(path);

	}

	private static Path getPath() {
		return ApplicationUtils.getApplication().getPath(PathType.STORE, SESSION_KEY);
	}
}
