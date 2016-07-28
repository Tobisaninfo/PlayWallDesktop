package de.tobias.playpad.launchpadplugin.impl;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.tobias.utils.util.FileUtils;

public class MapParser {

	public static Map<String, String> load(URL resource) throws IOException {
		Map<String, String> items = new HashMap<>();
		for (String line : FileUtils.readURL(resource).split("\n")) {
			String[] split = line.split("=");
			if (split.length == 2) {
				String color = split[0];
				String val = split[1];
				items.put(color, val);
			}
		}
		;
		return items;
	}
}
