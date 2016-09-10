package de.tobias.playpad.viewcontroller.main;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.project.Project;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class VolumeChangeListener implements ChangeListener<Number> {

	private Project openProject;

	public VolumeChangeListener(Project openProject) {
		this.openProject = openProject;
	}

	public void setOpenProject(Project openProject) {
		this.openProject = openProject;
	}

	@Override
	public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
		if (openProject != null) {
			for (Pad pad : openProject.getPads()) {
				if (pad != null && pad.getStatus() != PadStatus.EMPTY)
					pad.getContent().updateVolume();
			}
		}
	}
}
