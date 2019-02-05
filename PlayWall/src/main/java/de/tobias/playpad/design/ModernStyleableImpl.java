package de.tobias.playpad.design;

import de.tobias.playpad.PlayPadMain;
import javafx.stage.Stage;

public class ModernStyleableImpl implements Styleable {

	@Override
	public void applyStyleSheet(Stage stage) {
		PlayPadMain.getProgramInstance().getModernDesign().global().applyStyleSheet(stage);
	}
}
