package de.tobias.playpad.launchpadplugin.impl;

import de.thecodelabs.midi.feedback.FeedbackColor;
import de.thecodelabs.utils.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MapParser {

	private MapParser() {
	}

	public static Map<String, FeedbackColor> load(URL resource, Class<? extends Enum> type) throws IOException {
		Map<String, FeedbackColor> items = new HashMap<>();
		for (String line : IOUtils.readURL(resource).split("\n")) {
			line = line.trim();

			// Comment
			if (line.startsWith("%")) {
				continue;
			}

			String[] split = line.split("=");
			if (split.length == 2) {
				String color = split[0];
				Enum<?> val = Enum.valueOf(type, split[1]);
				items.put(color, (FeedbackColor) val);
			}
		}
		return items;
	}
}
