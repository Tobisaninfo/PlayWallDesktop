package de.tobias.playpad.design;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.design.modern.model.ModernGlobalDesign;
import de.tobias.playpad.profile.Profile;
import javafx.stage.Stage;

public class ModernStyleableImpl implements Styleable {

	@Override
	public void applyStyleSheet(Stage stage) {
		ModernGlobalDesign design = Profile.currentProfile().getProfileSettings().getDesign();
		PlayPadMain.getProgramInstance().getModernDesign().global().applyStyleSheet(design, stage);
	}
}
