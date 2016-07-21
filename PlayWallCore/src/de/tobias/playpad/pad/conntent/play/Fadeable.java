package de.tobias.playpad.pad.conntent.play;


public interface Fadeable {

	public void fadeIn();
	
	public void fadeOut(Runnable runnable);
	
	public boolean isFading();
}
