package de.tobias.playpad.project;

import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.ref.ProjectReference;
import org.dom4j.DocumentException;

import java.io.IOException;

/**
 * Created by tobias on 22.02.17.
 */
public interface ProjectReader {

	interface ProjectReaderDelegate {
		ProfileReference getProfileReference();
	}

	interface ProjectReaderListener {
		void readProject();
		void readMedia(String name);
		void totalMedia(int size);
	}

	Project read(ProjectReference projectReference, ProjectReaderDelegate delegate) throws IOException, DocumentException, ProjectNotFoundException;

}
