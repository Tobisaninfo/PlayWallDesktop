package de.tobias.playpad;

import de.tobias.utils.nui.NVC;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

public interface Displayable {

	StringProperty displayProperty();

	default Node getGraphics() {
		return null;
	}

	/**
	 * Optional Method for a displayable cell
	 *
	 * @return Einstellungen für dieses Objetkt.
	 */
	default NVC getSettingsViewController() {
		return null;
	}
}
