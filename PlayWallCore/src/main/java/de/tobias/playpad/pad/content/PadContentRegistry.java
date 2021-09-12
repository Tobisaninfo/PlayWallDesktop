package de.tobias.playpad.pad.content;

import de.tobias.playpad.registry.ComponentRegistry;
import de.tobias.playpad.registry.NoSuchComponentException;

import java.nio.file.Path;
import java.util.*;

public class PadContentRegistry extends ComponentRegistry<PadContentFactory> {

	public PadContentRegistry(String name) {
		super(name);
	}

	public List<PadContentFactory> getPadContentConnectsForFile(Path paths) throws NoSuchComponentException {
		return getPadContentConnectsForFiles(Collections.singletonList(paths));
	}

	public List<PadContentFactory> getPadContentConnectsForFiles(List<Path> paths) throws NoSuchComponentException {
		final Set<PadContentFactory> connects = new HashSet<>();
		for (String type : getTypes()) {
			PadContentFactory connect = getFactory(type);
			for (String extension : connect.getSupportedTypes()) {
				if (isExtensionMatchingAllFiles(extension, paths)) {
					connects.add(connect);
				}
			}
		}
		return new ArrayList<>(connects);
	}

	private boolean isExtensionMatchingAllFiles(String extension, List<Path> paths) {
		for (Path path : paths) {
			if (!path.getFileName().toString().toLowerCase().matches("." + extension)) {
				return false;
			}
		}
		return true;
	}

	public String[] getSupportedFileTypes() throws NoSuchComponentException {
		List<String> extensions = new ArrayList<>();
		for (String type : getTypes()) {
			PadContentFactory connect = getFactory(type);
			String[] fileExtensions = connect.getSupportedTypes();
			Collections.addAll(extensions, fileExtensions);
		}
		return extensions.toArray(new String[0]);
	}

}
