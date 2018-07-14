package de.tobias.playpad.pad.mediapath;

import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tobias on 20.03.17.
 */
public class MediaPool {

	private static final String mediaPoolFile = "media.db";

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
			e.printStackTrace();
		}
	}

	private Connection connection;

	private MediaPool() {
		App app = ApplicationUtils.getApplication();
		Path path = app.getPath(PathType.DOCUMENTS, mediaPoolFile);
		if (Files.notExists(path.getParent())) {
			try {
				Files.createDirectories(path.getParent());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + path.toString());

			PreparedStatement subjectCreateStmt = connection.prepareStatement(
					"CREATE TABLE IF NOT EXISTS `Path` (`id` VARCHAR NOT NULL PRIMARY KEY, `project` VARCHAR NOT NULL, `path` TEXT DEFAULT NULL);");
			subjectCreateStmt.execute();
			subjectCreateStmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Path getPath(MediaPath path) {
		if (connection != null) {
			PreparedStatement stmt = null;
			ResultSet result = null;
			try {
				stmt = connection.prepareStatement("SELECT * FROM Path WHERE id = ?");
				stmt.setString(1, path.getId().toString());
				result = stmt.executeQuery();

				if (result.next()) {
					String localPath = result.getString("path");
					if (localPath == null) {
						return null;
					}
					return Paths.get(localPath);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (result != null) {
					try {
						result.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	public void create(MediaPath path) {
		create(path, null);
	}

	public void create(MediaPath path, Path localPath) {
		if (connection != null) {
			PreparedStatement stmt = null;
			try {
				stmt = connection.prepareStatement("INSERT INTO Path VALUES (?, ?, ?)");
				stmt.setString(1, path.getId().toString());
				stmt.setString(2, path.getPad().getProject().getProjectReference().getUuid().toString());
				stmt.setString(3, localPath != null ? localPath.toString() : null);
				stmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	void setPath(MediaPath path, Path localPath) {
		if (connection != null) {
			if (getPath(path) == null) {
				create(path);
			}

			PreparedStatement stmt = null;
			try {
				stmt = connection.prepareStatement("UPDATE Path SET path = ? WHERE id = ?");
				stmt.setString(1, localPath.toString());
				stmt.setString(2, path.getId().toString());
				stmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			connection = null;
		}
	}

	// Find algorithm
	public static Path find(String filename, Path baseFolder, boolean includeSubdirectories) throws IOException {
		List<Path> result = new ArrayList<>();
		Files.newDirectoryStream(baseFolder).forEach(path -> {
			if (path.getFileName().toString().equals(filename)) {
				result.add(path);
			} else if (Files.isDirectory(path) && includeSubdirectories) {
				try {
					result.add(find(filename, path, false));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		return result.isEmpty() ? null : result.get(0);
	}
}
