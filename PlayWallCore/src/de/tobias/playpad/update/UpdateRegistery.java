package de.tobias.playpad.update;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import de.tobias.utils.util.OS;
import de.tobias.utils.util.SystemUtils;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;

public class UpdateRegistery {

	private static List<Updatable> updatables = new ArrayList<>();
	private static List<Updatable> availableUpdates = new ArrayList<>();

	public static void registerUpdateable(Updatable updatable) {
		updatables.add(updatable);
	}

	public static List<Updatable> getAvailableUpdates() {
		return updatables;
	}

	public static List<Updatable> lookupUpdates(UpdateChannel channel) {
		availableUpdates.clear();
		for (Updatable updatable : UpdateRegistery.updatables) {
			try {
				updatable.loadInformation(channel);
				if (updatable.isUpdateAvailable()) {
					availableUpdates.add(updatable);
				}
			} catch (IOException | URISyntaxException e) {

			}
		}
		return availableUpdates;
	}

	private static final String DOWNLOAD_PATH = "downloadPath";
	private static final String FILES = "files";
	private static final String LOCAL = "local";
	private static final String URL = "url";
	private static final String EXECUTE_FILE = "executePath";

	public static String buildParamaterString(String downloadPath) {
		JSONObject data = new JSONObject();
		data.put(DOWNLOAD_PATH, downloadPath);

		JSONArray array = new JSONArray();
		for (Updatable updatable : availableUpdates) {
			JSONObject file = new JSONObject();
			file.put(URL, updatable.getDownloadPath().toString());
			file.put(LOCAL, updatable.getLocalPath().toString());
			array.add(file);
		}
		data.put(FILES, array);
		try {
			data.put(EXECUTE_FILE, SystemUtils.getRunPath().toString());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		String json = data.toJSONString(JSONStyle.MAX_COMPRESS);
		return json;
	}

	public static boolean needsAdminPermission() {
		for (Updatable updatable : availableUpdates) {
			if (!Files.isWritable(updatable.getLocalPath())) {
				return true;
			}
			if (OS.isWindows()) {
				try {
					if (Files.getOwner(updatable.getLocalPath()).getName().toLowerCase().contains("admin")) {
						return true;
					}
				} catch (IOException e) {}
			}
		}
		return false;
	}
}
