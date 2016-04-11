package de.tobias.playpad.view;

import java.util.function.Consumer;

import de.tobias.playpad.DisplayableColor;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;

public class ColorView extends GridPane {

	public ColorView(DisplayableColor startColor, DisplayableColor[] colors, Consumer<DisplayableColor> finish) {
		double size = Math.sqrt(colors.length);
		int iSize = (int) size;
		if (size != iSize) {
			iSize++;
		}

		setVgap(5);
		setHgap(5);

		setPadding(new Insets(5));

		int index = 0;
		for (int y = 0; y < iSize; y++) {
			for (int x = 0; x < iSize; x++) {
				if (index < colors.length) {
					DisplayableColor color = colors[index++];

					// Style in CSS
					Rectangle rectangle = new Rectangle(40, 40);
					rectangle.setFill(color.getPaint());

					rectangle.getStyleClass().add("color-view-item");

					// Gestrichelte Linie
					if (color == startColor) {
						rectangle.getStrokeDashArray().addAll(3.0);
					}

					// EventHandler
					rectangle.setOnMouseReleased(event -> finish.accept(color));
					add(rectangle, x, y);
				}
			}
		}
	}
}
