package de.tobias.playpad.pad.mediapath;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by tobias on 20.03.17.
 */
public class MediaPool {

	private static final String MEDIA_DB = "media.db";

	private static MediaPool instance;

	public static MediaPool getInstance() {
		if (instance == null) {
			instance = new MediaPool();
		}
		return instance;
	}

	static {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			Logger.error(e);
		}
	}

	private Connection connection;

	private MediaPool() {
		App app = ApplicationUtils.getApplication();
		Path path = app.getPath(PathType.DOCUMENTS, MEDIA_DB);
		if (Files.notExists(path.getParent())) {
			try {
				Files.createDirectories(path.getParent());
			} catch (IOException e) {
				Logger.error(e);
			}
		}

		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + path.toString());
		} catch (SQLException e) {
			Logger.error(e);
			return;
		}

		try (PreparedStatement subjectCreateStmt = connection.prepareStatement(
				"CREATE TABLE IF NOT EXISTS `Path` (`id` VARCHAR NOT NULL PRIMARY KEY, `project` VARCHAR NOT NULL, `path` TEXT DEFAULT NULL);")) {
			subjectCreateStmt.execute();
		} catch (SQLException e) {
			Logger.error(e);
		}
	}

	public List<MediaPath> getMediaPathsForProject(UUID projectId) {
		List<MediaPath> paths = new ArrayList<>();

		if (connection == null) {
			return paths;
		}

		try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Path WHERE project = ?")) {
			stmt.setString(1, projectId.toString());

			try (ResultSet result = stmt.executeQuery()) {
				while (result.next()) {
					UUID id = UUID.fromString(result.getString("id"));
					String localPath = result.getString("path");

					MediaPath path = new MediaPath(id, localPath, null);
					paths.add(path);
				}
			}
		} catch (SQLException e) {
			Logger.error(e);
		}
		return paths;
	}

	public Path getPath(MediaPath path) {
		if (connection == null) {
			return null;
		}
		try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM Path WHERE id = ?")) {
			stmt.setString(1, path.getId().toString());

			try (ResultSet result = stmt.executeQuery()) {
				if (result.next()) {
					String localPath = result.getString("path");
					if (localPath == null) {
						return null;
					}
					return Paths.get(localPath);
				}
			}
		} catch (SQLException e) {
			Logger.error(e);
		}
		return null;
	}

	public void create(MediaPath path) {
		create(path, null);
	}

	public void create(MediaPath path, Path localPath) {
		if (connection != null) {
			try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO Path VALUES (?, ?, ?)")) {
				stmt.setString(1, path.getId().toString());
				stmt.setString(2, path.getPad().getProject().getProjectReference().getUuid().toString());
				stmt.setString(3, localPath != null ? localPath.toString() : null);
				stmt.executeUpdate();
			} catch (SQLException e) {
				Logger.error(e);
			}
		}
	}

	void setPath(MediaPath path, Path localPath) {
		if (connection != null) {
			if (getPath(path) == null) {
				create(path);
			}

			try (PreparedStatement stmt = connection.prepareStatement("UPDATE Path SET path = ? WHERE id = ?")) {
				stmt.setString(1, localPath.toString());
				stmt.setString(2, path.getId().toString());
				stmt.executeUpdate();
			} catch (SQLException e) {
				Logger.error(e);
			}
		}
	}

	public void deleteEntries(UUID projectId) {
		if (connection != null) {
			try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM Path WHERE project = ?")) {
				stmt.setString(1, projectId.toString());
				stmt.executeUpdate();
			} catch (SQLException e) {
				Logger.error(e);
			}
		}
	}

	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			Logger.error(e);
		} finally {
			connection = null;
		}
	}

	// Find algorithm
	public static Path find(String filename, Path baseFolder, boolean includeSubdirectories) throws IOException {
		List<Path> result = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseFolder)) {
			stream.forEach(path -> {
				if (path.getFileName().toString().equals(filename)) {
					result.add(path);
				} else if (Files.isDirectory(path) && includeSubdirectories) {
					try {
						result.add(find(filename, path, false));
					} catch (IOException e) {
						Logger.error(e);
					}
				}
			});
		}
		return result.isEmpty() ? null : result.get(0);
	}
}
