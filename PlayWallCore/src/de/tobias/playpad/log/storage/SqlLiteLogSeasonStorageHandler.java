package de.tobias.playpad.log.storage;

import de.tobias.logger.LogLevel;
import de.tobias.logger.Logger;
import de.tobias.playpad.log.LogItem;
import de.tobias.playpad.log.LogSeason;
import de.tobias.playpad.log.PlayOutItem;

import java.nio.file.Path;
import java.sql.*;

public class SqlLiteLogSeasonStorageHandler implements LogSeasonStorageHandler {

	private Connection connection;

	public SqlLiteLogSeasonStorageHandler(Path path) throws SQLException {
		connection = DriverManager.getConnection("jdbc:sqlite:" + path.toString());

		if (connection != null) {
			createDatabaseTable("CREATE TABLE IF NOT EXISTS LogSeason (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, name VARCHAR NOT NULL);");
			createDatabaseTable("CREATE TABLE IF NOT EXISTS LogItem (uuid VARCHAR NOT NULL, name VARCHAR NOT NULL, color VARCHAR NOT NULL, page INTEGER NOT NULL, position INTEGER NOT NULL, logSeason INTEGER NOT NULL, PRIMARY KEY (uuid, logSeason), FOREIGN KEY (logSeason) REFERENCES LogSeason(id));");
			createDatabaseTable("CREATE TABLE IF NOT EXISTS PlayOutItem (uuid VARCHAR NOT NULL, time INTEGER NOT NULL, logSeason INTEGER NOT NULL, PRIMARY KEY (uuid, logSeason, time), FOREIGN KEY (uuid) REFERENCES LogItem(uuid));");
		}
	}

	private void createDatabaseTable(String statement) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement(statement);
		stmt.execute();
		stmt.close();
	}

	@Override
	public void addLogSeason(LogSeason season) {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = connection.prepareStatement("INSERT INTO LogSeason (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, season.getName());
			int affectedRows = stmt.executeUpdate();

			if (affectedRows == 0) {
				throw new SQLException("Creating user failed, no rows affected.");
			}

			rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				season.setId(rs.getInt(1));
			} else {
				throw new SQLException("Creating MeasurePoint failed, no ID obtained.");
			}
		} catch (SQLException e) {
			Logger.log(LogLevel.ERROR, e.getLocalizedMessage());
		} finally {
			closeResource(rs);
			closeResource(stmt);
		}
	}

	@Override
	public void addLogItem(LogItem item) {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement("INSERT INTO LogItem VALUES (?, ?, ?, ?, ?, ?)");
			stmt.setString(1, item.getUuid().toString());
			stmt.setString(2, item.getName());
			stmt.setString(3, item.getColor());
			stmt.setInt(4, item.getPage());
			stmt.setInt(5, item.getPosition());
			stmt.setInt(6, item.getLogSeason().getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			Logger.log(LogLevel.ERROR, e.getLocalizedMessage());
		} finally {
			closeResource(stmt);
		}
	}

	@Override
	public void addPlayOutItem(PlayOutItem item) {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement("INSERT INTO PlayOutItem VALUES (?, ?, ?)");
			stmt.setString(1, item.getPathUuid().toString());
			stmt.setInt(3, item.getLogSeason().getId());
			stmt.setLong(2, item.getTime());
			stmt.executeUpdate();
		} catch (SQLException e) {
			Logger.log(LogLevel.ERROR, e.getLocalizedMessage());
		} finally {
			closeResource(stmt);
		}
	}


	private void closeResource(AutoCloseable stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void close() throws RuntimeException {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
