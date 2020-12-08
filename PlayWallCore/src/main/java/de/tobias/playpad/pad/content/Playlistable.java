package de.tobias.playpad.pad.content;

import de.tobias.playpad.pad.mediapath.MediaPath;
import javafx.beans.property.IntegerProperty;

public interface Playlistable {
	int getCurrentPlayingMediaIndex();

	IntegerProperty currentPlayingMediaIndexProperty();

	boolean hasNext();

	void next();

	boolean isLoaded(MediaPath mediaPath);
}
