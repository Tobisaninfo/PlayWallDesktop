package de.tobias.playpad.pad.listener;

import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.project.Project;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * Created by tobias on 24.03.17.
 */
public class PadStatusNotFoundListener implements ChangeListener<PadStatus> {

	private Project project;

	public PadStatusNotFoundListener(Project project) {
		this.project = project;
	}

	@Override
	public void changed(ObservableValue<? extends PadStatus> observable, PadStatus oldValue, PadStatus newValue) {
		if (newValue == PadStatus.NOT_FOUND) {
			project.updateNotFoundProperty();
		} else if (oldValue == PadStatus.NOT_FOUND) {
			project.updateNotFoundProperty();
		}
	}
}
