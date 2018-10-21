package de.tobias.playpad.design;

import de.tobias.playpad.DisplayableColor;
import de.tobias.playpad.design.modern.ModernCartDesign;
import javafx.scene.Node;

import java.util.function.Consumer;

/**
 * Wenn vom Design unterstützt, wird hier die GUI für Farbeinstellungen erstellt.
 *
 * @author tobias
 */
public interface ColorModeHandler {

	Node getColorInterface(Consumer<DisplayableColor> onSelection);

	void setColor(ModernCartDesign design, DisplayableColor color);
}
