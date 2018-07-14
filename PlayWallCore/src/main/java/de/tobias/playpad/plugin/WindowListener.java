package de.tobias.playpad.plugin;

public interface WindowListener<T> {

	void onInit(T t);

	default void onClose(T t) {

	}
}
