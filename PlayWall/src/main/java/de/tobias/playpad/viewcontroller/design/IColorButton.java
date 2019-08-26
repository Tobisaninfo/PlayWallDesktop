package de.tobias.playpad.viewcontroller.design;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import javafx.geometry.Pos;
import javafx.scene.control.Labeled;

public interface IColorButton {

	default void addIconToButton(Labeled button) {
		FontIcon iconDefault = new FontIcon(FontAwesomeType.ARROW_CIRCLE_DOWN);
		iconDefault.getStyleClass().remove(FontIcon.STYLE_CLASS);
		button.setGraphic(iconDefault);
		button.setAlignment(Pos.CENTER_RIGHT);
	}

}
