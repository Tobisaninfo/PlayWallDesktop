package de.tobias.playpad.project;

import java.nio.file.Path;

public interface Importable {

	public String replaceProfile(String name);

	public String replaceProject(String name);

	public Path mediaFolder();
}