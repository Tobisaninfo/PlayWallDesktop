package de.tobias.playpad.pad.fade;


public interface Fadeable {

	void fadeIn();

	void fadeOut(Runnable runnable);

	boolean isFadeActive();
}
