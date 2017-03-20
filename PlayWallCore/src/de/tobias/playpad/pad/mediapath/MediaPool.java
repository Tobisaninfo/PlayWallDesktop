package de.tobias.playpad.pad.mediapath;

import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

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
					"CREATE TABLE IF NOT EXISTS `Path` (`id` VARCHAR NOT NULL PRIMARY KEY, `path` TEXT DEFAULT NULL);");
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
					return Paths.get(result.getString("path"));
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
		if (connection != null) {
			PreparedStatement stmt = null;
			try {
				stmt = connection.prepareStatement("INSERT INTO Path VALUES (?, ?)");
				stmt.setString(1, path.getId().toString());
				stmt.setString(2, path.getPath().toString());
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

	public void setPath(MediaPath path) {
		if (connection != null) {
			PreparedStatement stmt = null;
			try {
				stmt = connection.prepareStatement("UPDATE Path SET path = ?");
				stmt.setString(1, path.getId().toString());
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
}
