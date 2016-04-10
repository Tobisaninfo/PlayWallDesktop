package de.tobias.playpad.plugin;

public interface WindowListener<T> {

	public void onInit(T t);

	public default void onClose(T t) {
		
	}
}
