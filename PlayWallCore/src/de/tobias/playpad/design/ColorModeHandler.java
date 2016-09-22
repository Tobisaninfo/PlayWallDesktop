package de.tobias.playpad.design;

import java.util.function.Consumer;

import de.tobias.playpad.DisplayableColor;
import javafx.scene.Node;

/**
 * Wenn vom Design unterstützt, wird hier die GUI für Farbeinstellungen erstellt.
 * 
 * @author tobias
 *
 */
public interface ColorModeHandler {

	public Node getColorInterface(Consumer<DisplayableColor> onSelection);

	public void setColor(CartDesign design, DisplayableColor color);
}
