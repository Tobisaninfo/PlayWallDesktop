package de.tobias.playpad.plugin.playout.storage;

import de.thecodelabs.logger.LogLevel;
import de.thecodelabs.logger.Logger;
import de.tobias.playpad.plugin.playout.log.LogItem;
import de.tobias.playpad.plugin.playout.log.LogSeason;
import de.tobias.playpad.plugin.playout.log.PlayOutItem;
import de.tobias.playpad.plugin.playout.log.storage.LogSeasonStorageHandler;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqlLiteLogSeasonStorageHandler implements LogSeasonStorageHandler {

	private Connection connection;

	public SqlLiteLogSeasonStorageHandler(Path path) throws SQLException {
		connection = DriverManager.getConnection("jdbc:sqlite:" + path.toString());

		createDatabaseTable("CREATE TABLE IF NOT EXISTS LogSeason (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, name VARCHAR NOT NULL, columns INTEGER NOT NULL, rows INTEGER NOT NULL);");
		createDatabaseTable("CREATE TABLE IF NOT EXISTS LogItem (uuid VARCHAR NOT NULL, name VARCHAR NOT NULL, color VARCHAR NOT NULL, page INTEGER NOT NULL, position INTEGER NOT NULL, logSeason INTEGER NOT NULL, PRIMARY KEY (uuid, logSeason), FOREIGN KEY (logSeason) REFERENCES LogSeason(id));");
		createDatabaseTable("CREATE TABLE IF NOT EXISTS PlayOutItem (uuid VARCHAR NOT NULL, time INTEGER NOT NULL, logSeason INTEGER NOT NULL, PRIMARY KEY (uuid, logSeason, time), FOREIGN KEY (uuid) REFERENCES LogItem(uuid));");
	}

	private void createDatabaseTable(String statement) throws SQLException {
		try (PreparedStatement stmt = connection.prepareStatement(statement)) {
			stmt.execute();
		}
	}

	@Override
	public void addLogSeason(LogSeason season) {
		try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO LogSeason (name, columns, rows) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, season.getName());
			stmt.setInt(2, season.getColumns());
			stmt.setInt(3, season.getRows());
			int affectedRows = stmt.executeUpdate();

			if (affectedRows == 0) {
				throw new SQLException("Creating log season failed, no rows affected.");
			}

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				if (rs.next()) {
					season.setId(rs.getInt(1));
				} else {
					throw new SQLException("Creating log season failed, no ID obtained.");
				}
			}
		} catch (SQLException e) {
			Logger.log(LogLevel.ERROR, e.getLocalizedMessage());
		}
	}

	@Override
	public void addLogItem(LogItem item) {
		try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO LogItem VALUES (?, ?, ?, ?, ?, ?)")) {
			stmt.setString(1, item.getUuid().toString());
			stmt.setString(2, item.getName());
			stmt.setString(3, item.getColor());
			stmt.setInt(4, item.getPage());
			stmt.setInt(5, item.getPosition());
			stmt.setInt(6, item.getLogSeason().getId());

			stmt.executeUpdate();
		} catch (SQLException e) {
			Logger.error(e);
		}
	}

	@Override
	public void addPlayOutItem(PlayOutItem item) {
		try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO PlayOutItem VALUES (?, ?, ?)")) {
			stmt.setString(1, item.getPathUuid().toString());
			stmt.setInt(3, item.getLogSeason().getId());
			stmt.setLong(2, item.getTime());
			stmt.executeUpdate();
		} catch (SQLException e) {
			Logger.error(e);
		}
	}

	@Override
	public List<LogSeason> getAllLogSeasonsLazy() {
		List<LogSeason> logSeasons = new ArrayList<>();

		try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM LogSeason")) {
			try (ResultSet resultSet = stmt.executeQuery()) {

				while (resultSet.next()) {
					LogSeason logSeason = new LogSeason(
							resultSet.getInt("id"),
							resultSet.getString("name"),
							resultSet.getInt("columns"),
							resultSet.getInt("rows")
					);
					logSeasons.add(logSeason);
				}
			}
		} catch (SQLException e) {
			Logger.error(e);
		}
		return logSeasons;
	}

	@Override
	public LogSeason getLogSeason(int id) {
		try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM LogSeason WHERE id = ?")) {
			stmt.setInt(1, id);

			try (ResultSet resultSet = stmt.executeQuery()) {
				if (resultSet.next()) {
					LogSeason logSeason = new LogSeason(
							resultSet.getInt("id"),
							resultSet.getString("name"),
							resultSet.getInt("columns"),
							resultSet.getInt("rows")
					);
					logSeason.getLogItems().addAll(getAllPlayoutItems(logSeason));
					return logSeason;
				}
			}
		} catch (SQLException e) {
			Logger.log(LogLevel.ERROR, e.getLocalizedMessage());
		}
		return null;
	}

	private List<LogItem> getAllPlayoutItems(LogSeason season) {
		List<LogItem> logItems = new ArrayList<>();
		try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM LogItem WHERE logSeason = ?")) {
			stmt.setInt(1, season.getId());
			try (ResultSet resultSet = stmt.executeQuery()) {

				while (resultSet.next()) {
					LogItem logItem = new LogItem(
							UUID.fromString(resultSet.getString("uuid")),
							resultSet.getString("name"),
							resultSet.getString("color"),
							resultSet.getInt("page"),
							resultSet.getInt("position"),
							season
					);
					logItem.getPlayOutItems().addAll(getAllPlayoutItems(logItem));
					logItems.add(logItem);
				}
			}
		} catch (SQLException e) {
			Logger.log(LogLevel.ERROR, e.getLocalizedMessage());
		}
		return logItems;
	}

	private List<PlayOutItem> getAllPlayoutItems(LogItem item) {
		List<PlayOutItem> playOutItems = new ArrayList<>();
		try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM PlayOutItem WHERE uuid = ? AND logSeason = ?")) {
			stmt.setString(1, item.getUuid().toString());
			stmt.setInt(2, item.getLogSeason().getId());

			try (ResultSet resultSet = stmt.executeQuery()) {

				while (resultSet.next()) {
					PlayOutItem playOutItem = new PlayOutItem(
							UUID.fromString(resultSet.getString("uuid")),
							item.getLogSeason(),
							resultSet.getLong("time")
					);
					playOutItems.add(playOutItem);
				}
			}
		} catch (SQLException e) {
			Logger.log(LogLevel.ERROR, e.getLocalizedMessage());
		}
		return playOutItems;
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
