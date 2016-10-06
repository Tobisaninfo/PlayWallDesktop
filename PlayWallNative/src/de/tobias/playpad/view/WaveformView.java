package de.tobias.playpad.view;

import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class WaveformView extends Path {

	public WaveformView(float[] data) {
		getElements().add(new MoveTo(0, 0));

		double width2 = data.length / 1200.0;
		int width = data.length / 10000;
		System.out.println(data.length);
		System.out.println(width);

		int i = 0;
		for (i = 0; i < data.length; i += width) {
			if (i < data.length) {
				LineTo lineTo = new LineTo(i / width2, data[i] * 50.0);
				MoveTo moveTo = new MoveTo(i / width2, data[i] * 50.0);

				getElements().addAll(lineTo, moveTo);
			}
		}
		for (; i >= 0; i -= width) {
			if (i >= 0 && i < data.length) {
				LineTo lineTo = new LineTo(i / width2, -data[i] * 50.0);
				MoveTo moveTo = new MoveTo(i / width2, -data[i] * 50.0);

				getElements().addAll(lineTo, moveTo);
			}
		}
		getElements().add(new LineTo(0, 0));
		getElements().add(new MoveTo(0, 0));
		setFill(Color.BLACK);
	}
}
