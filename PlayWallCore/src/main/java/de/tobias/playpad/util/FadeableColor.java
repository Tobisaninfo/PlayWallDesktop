package de.tobias.playpad.util;

import javafx.animation.Interpolatable;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;

public class FadeableColor implements Interpolatable<FadeableColor> {

	private List<Stop> stops;

	public FadeableColor(Color color) {
		stops = new ArrayList<>();
		stops.add(new Stop(0, color));
		stops.add(new Stop(1, color));
	}

	public FadeableColor(String color) {
		this(Color.web(color));
	}

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

		List<Stop> stopList = new ArrayList<>();
		for (int i = 0; i < this.getStops().size(); i++) {
			Color startColor = this.getStops().get(i).getColor();
			Color endColor = endValue.getStops().get(i).getColor();

			double red = startColor.getRed() + (endColor.getRed() - startColor.getRed()) * t;
			double green = startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * t;
			double blue = startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * t;
			double opacity = startColor.getOpacity() + (endColor.getOpacity() - startColor.getOpacity()) * t;

			Color color = new Color(red, green, blue, opacity);
			Stop stop = new Stop(this.getStops().get(i).getOffset(), color);
			stopList.add(stop);
		}

		return new FadeableColor(stopList);
	}

	@Override
	public String toString() {
		StringBuilder val = new StringBuilder("linear-gradient(");
		for (int i = 0; i < stops.size(); i++) {
			val.append(stops.get(i).getColor().toString().replace("0x", "#"));
			if (i + 1 < stops.size()) {
				val.append(", ");
			}
		}
		val.append(")");
		return val.toString();
	}
}
