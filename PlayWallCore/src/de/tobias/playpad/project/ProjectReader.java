package de.tobias.playpad.project;

import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.settings.ProfileNotFoundException;
import org.dom4j.DocumentException;

import java.io.IOException;

/**
 * Created by tobias on 22.02.17.
 */
public interface ProjectReader {

	interface ProjectReaderDelegate {
		ProfileReference getProfileReference();
	}

	Project read(ProjectReference projectReference, ProjectReaderDelegate delegate) throws IOException, DocumentException, ProfileNotFoundException, ProjectNotFoundException;

}
