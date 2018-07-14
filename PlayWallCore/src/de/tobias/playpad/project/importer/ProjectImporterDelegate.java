package de.tobias.playpad.project.importer;

import java.nio.file.Path;

/**
 * Created by tobias on 11.03.17.
 */
public interface ProjectImporterDelegate {

	String getProjectName();

	boolean shouldProjectSynced();

	boolean shouldImportProfile();

	String getProfileName();

	boolean shouldImportMedia();

	Path getMediaPath();
}
