package de.tobias.playpad.pad.content;

import javafx.beans.property.IntegerProperty;

public interface Playlistable {
	int getCurrentPlayingMediaIndex();

	IntegerProperty currentPlayingMediaIndexProperty();

	void next();
}
