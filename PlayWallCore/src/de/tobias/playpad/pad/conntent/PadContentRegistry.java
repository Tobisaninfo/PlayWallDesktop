package de.tobias.playpad.pad.conntent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Deprecated
public class PadContentRegistry {

	private static HashMap<String, PadContentConnect> padContents;

	static {
		padContents = new HashMap<>();
	}

	public static void registerActionConnect(PadContentConnect connect) {
		padContents.put(connect.getType(), connect);
	}

	public static PadContentConnect getPadContentConnect(String type) throws UnkownPadContentException {
		if (padContents.containsKey(type)) {
			return padContents.get(type);
		} else {
			throw new UnkownPadContentException(type);
		}
	}

	public static Set<String> getTypes() {
		return padContents.keySet();
	}

	public static String getContentTypeForFile(Path path) {
		for (String type : padContents.keySet()) {
			PadContentConnect connect = padContents.get(type);
			String[] fileExtensions = connect.getSupportedTypes();
			for (String fileExtension : fileExtensions) {
				if (path.toString().toLowerCase().matches("." + fileExtension)) {
					return type;
				}
			}
		}
		return null;
	}

	// TODO
	public static Set<PadContentConnect> getPadContentConnectsForFile(Path path) throws UnkownPadContentException {
		Set<PadContentConnect> connects = new HashSet<>();
		for (String type : PadContentRegistry.getTypes()) {
			PadContentConnect connect = PadContentRegistry.getPadContentConnect(type);
			for (String extension : connect.getSupportedTypes()) {
				if (path.getFileName().toString().toLowerCase().matches("." + extension)) {
					connects.add(connect);
				}
			}
		}
		return connects;
	}

	public static String[] getSupportedFileTypes() {
		List<String> extensions = new ArrayList<>();
		for (String type : padContents.keySet()) {
			PadContentConnect connect = padContents.get(type);
			String[] fileExtensions = connect.getSupportedTypes();
			Collections.addAll(extensions, fileExtensions);
		}
		return extensions.toArray(new String[extensions.size()]);
	}
}
