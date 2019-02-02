package de.tobias.playpad.design;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.design.modern.DesignHandler;
import de.tobias.playpad.design.modern.ModernGlobalDesign;
import de.tobias.playpad.profile.Profile;
import javafx.stage.Stage;

public class ModernDesignHandler implements DesignHandler {

	@Override
	public void applyStyleSheet(Stage stage) {
		ModernGlobalDesign design = Profile.currentProfile().getProfileSettings().getDesign();
		PlayPadMain.getProgramInstance().getModernDesign().getModernGlobalDesignHandler().applyStyleSheet(design, stage);
	}
}
