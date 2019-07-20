package de.tobias.playpad.action.settings;

import de.thecodelabs.midi.Mapping;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playpad.Displayable;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

public interface ActionSettingsEntry extends Displayable {

	String getName();

	FontIcon getIcon();

	NVC getDetailSettingsController(Mapping mapping, IMappingTabViewController controller);

	@Override
	default Node getGraphics() {
		return getIcon();
	}

	@Override
	default StringProperty displayProperty() {
		return new SimpleStringProperty(getName());
	}
}
