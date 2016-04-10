package de.tobias.playpad.pad.conntent;


public interface Fadeable {

	public void fadeIn();
	
	public void fadeOut(Runnable runnable);
	
	public boolean isFading();
}
