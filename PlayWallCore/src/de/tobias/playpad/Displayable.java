package de.tobias.playpad;

import de.tobias.utils.ui.ContentViewController;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

public interface Displayable {

	public StringProperty displayProperty();

	public default Node getGraphics() {
		return null;
	}

	/**
	 * Optional Method for a displayable cell
	 * 
	 * @return Einstellungen für dieses Objetkt.
	 */
	public default ContentViewController getSettingsViewController() {
		return null;
	}
}
