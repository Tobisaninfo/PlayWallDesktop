package de.tobias.playpad.layout;

import java.util.ArrayList;
import java.util.List;

import javafx.animation.Interpolatable;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;

public class FadeableColor implements Interpolatable<FadeableColor> {

	private List<Stop> stops;

	public FadeableColor(Color hi, Color low) {
		stops = new ArrayList<>();
		stops.add(new Stop(0, hi));
		stops.add(new Stop(1, low));
	}

	public FadeableColor(String colorHi, String colorLow) {
		this(Color.web(colorHi), Color.web(colorLow));
	}

	public FadeableColor(List<Stop> stops) {
		this.stops = stops;
	}

	public List<Stop> getStops() {
		return stops;
	}

	@Override
	public FadeableColor interpolate(FadeableColor endValue, double t) {
		if (t <= 0.0)
			return this;
		if (t >= 1.0)
			return endValue;

		FadeableColor current = this;
		List<Stop> stops = new ArrayList<>();
		for (int i = 0; i < current.getStops().size(); i++) {
			Color startColor = current.getStops().get(i).getColor();
			Color endColor = endValue.getStops().get(i).getColor();

			double red = startColor.getRed() + (endColor.getRed() - startColor.getRed()) * t;
			double green = startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * t;
			double blue = startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * t;
			double opacity = startColor.getOpacity() + (endColor.getOpacity() - startColor.getOpacity()) * t;

			Color color = new Color(red, green, blue, opacity);
			Stop stop = new Stop(current.getStops().get(i).getOffset(), color);
			stops.add(stop);
		}

		return new FadeableColor(stops);
	}

	@Override
	public String toString() {
		String val = "linear-gradient(";
		for (int i = 0; i < stops.size(); i++) {
			val += stops.get(i).getColor().toString().replace("0x", "#");
			if (i + 1 < stops.size()) {
				val += ", ";
			}
		}
		val += ")";
		return val;
	}
}
