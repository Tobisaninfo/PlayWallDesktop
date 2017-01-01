package de.tobias.playpad.pad.conntent.play;


public interface Fadeable {

	void fadeIn();

	void fadeOut(Runnable runnable);
	
	boolean getFade();
}
