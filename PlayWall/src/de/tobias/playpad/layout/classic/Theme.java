package de.tobias.playpad.layout.classic;

import javafx.scene.paint.Color;

public enum Theme {

	DARK("Dark.css", Color.RED, Color.rgb(40, 40, 40)),
	TWILIGHT("Twilight.css", Color.rgb(200, 200, 255), Color.rgb(40, 40, 50)),
	LIGHT("Light.css", Color.BLACK, Color.TRANSPARENT);

	private String name;
	private Color color;
	private Color backgrond;

	private Theme(String name, Color color, Color background) {
		this.name = name;
		this.color = color;
		this.backgrond = background;
	}

	public Color getGridColor() {
		return color;
	}

	public Color getBackground() {
		return backgrond;
	}

	public String getName() {
		return name;
	}

	public String getCss() {
		return "de/tobias/playpad/assets/style/" + name;
	}

}
