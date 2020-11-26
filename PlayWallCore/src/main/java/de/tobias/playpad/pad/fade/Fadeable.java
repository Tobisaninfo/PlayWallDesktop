package de.tobias.playpad.pad.fade;


import javafx.util.Duration;

public interface Fadeable {

	void fadeIn();

	default void fadeOut() {
		fadeOut(null);
	}

	void fadeOut(Runnable runnable);

	boolean isFadeActive();

	void fade(double from, double to, Duration duration, Runnable onFinish);
}
