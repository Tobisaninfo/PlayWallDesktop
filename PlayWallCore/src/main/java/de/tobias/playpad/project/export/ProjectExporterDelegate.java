package de.tobias.playpad.project.export;

/**
 * Created by tobias on 11.03.17.
 */
public interface ProjectExporterDelegate {

	void taskComplete();

	void setTasks(int amount);
}
