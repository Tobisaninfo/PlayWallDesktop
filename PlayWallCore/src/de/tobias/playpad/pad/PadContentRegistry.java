package de.tobias.playpad.pad;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.registry.ComponentRegistry;
import de.tobias.playpad.registry.NoSuchComponentException;

public class PadContentRegistry extends ComponentRegistry<PadContentConnect> {

	public PadContentRegistry(String name) {
		super(name);
	}

	public Set<PadContentConnect> getPadContentConnectsForFile(Path path) throws NoSuchComponentException {
		Set<PadContentConnect> connects = new HashSet<>();
		for (String type : getTypes()) {
			PadContentConnect connect = getComponent(type);
			for (String extension : connect.getSupportedTypes()) {
				if (path.getFileName().toString().toLowerCase().matches("." + extension)) {
					connects.add(connect);
				}
			}
		}
		return connects;
	}

	public String[] getSupportedFileTypes() throws NoSuchComponentException {
		List<String> extensions = new ArrayList<>();
		for (String type : getTypes()) {
			PadContentConnect connect = getComponent(type);
			String[] fileExtensions = connect.getSupportedTypes();
			Collections.addAll(extensions, fileExtensions);
		}
		return extensions.toArray(new String[extensions.size()]);
	}

}
