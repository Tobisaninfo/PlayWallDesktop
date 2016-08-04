package de.tobias.playpad.viewcontroller.main;

import java.util.List;

public class LayoutChangedListener {

	public void handle(List<Runnable> runnables) {
		for (Runnable run : runnables) {
			run.run();
		}
	}
}
