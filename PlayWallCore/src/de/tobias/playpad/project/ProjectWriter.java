package de.tobias.playpad.project;

import java.io.IOException;

/**
 * Created by tobias on 26.02.17.
 */
public interface ProjectWriter {
	void write(Project project) throws IOException;
}
