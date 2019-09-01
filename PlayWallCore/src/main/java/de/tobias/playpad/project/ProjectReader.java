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
		ProfileReference getProfileReference() throws ProfileAbortException;

		class ProfileAbortException extends Exception {
		}
	}

	interface ProjectReaderListener {
		void startReadProject();

		void finishReadProject();

		void readMedia(String name);

		void totalMedia(int size);

		void finish();

		void abort();
	}

	Project read(ProjectReference projectReference, ProjectReaderDelegate delegate) throws IOException, DocumentException, ProjectNotFoundException;

}
