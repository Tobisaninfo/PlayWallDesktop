package de.tobias.playpad.viewcontroller.dialog.project;

import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.ProjectReader;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.viewcontroller.dialog.profile.ProfileChooseDialog;
import de.tobias.utils.nui.NVCStage;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Created by tobias on 18.03.17.
 */
public class ProjectReaderDelegateImpl implements ProjectReader.ProjectReaderDelegate {

	public static ProjectReader.ProjectReaderDelegate getInstance(Window owner) {
		return new ProjectReaderDelegateImpl(owner);
	}

	private Window owner;

	private ProjectReaderDelegateImpl(Window owner) {
		this.owner = owner;
	}

	// Show a dialog to choose a new profile
	@Override
	public ProfileReference getProfileReference() {
		ProfileChooseDialog dialog = new ProfileChooseDialog(owner);

		dialog.getStageContainer().ifPresent(NVCStage::showAndWait);
		Optional<Profile> profile = dialog.showAndWait();
		return profile.map(Profile::getRef).orElse(null);
	}
}
