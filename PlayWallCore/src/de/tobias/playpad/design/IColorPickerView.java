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
public interface IColorPickerView {

	public Node getColorInterface(Consumer<DisplayableColor> onSelection);

}
