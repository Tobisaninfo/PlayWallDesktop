package de.tobias.playpad.registry;

import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.icon.FontIconType;
import de.tobias.playpad.Displayable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

/**
 * Created by tobias on 31.12.16.
 */
public class Component implements Displayable {

	private String type;
	private StringProperty displayProperty;
	private FontIcon graphics;

	public Component(String type) {
		this.type = type;
		this.displayProperty = new SimpleStringProperty();
		this.graphics = new FontIcon();
	}

	public String getType() {
		return type;
	}

	@Override
	public StringProperty displayProperty() {
		return displayProperty;
	}

	@Override
	public Node getGraphics() {
		return graphics;
	}

	public void setName(String name) {
		displayProperty.set(name);
	}

	public void setGraphics(FontIconType icon) {
		this.graphics = new FontIcon(icon);
	}

	public void setGraphics(FontIconType icon, int size) {
		this.graphics = new FontIcon(icon.getFontFile(), icon);
		this.graphics.setSize(size);
	}

	@Override
	public String toString() {
		return displayProperty.get();
	}
}
