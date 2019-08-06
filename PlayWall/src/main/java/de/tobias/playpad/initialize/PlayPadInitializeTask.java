package de.tobias.playpad.initialize;

import de.thecodelabs.utils.application.App;
import de.tobias.playpad.PlayPadImpl;

public interface PlayPadInitializeTask {
	String name();
	void run(App app, PlayPadImpl instance);
}
