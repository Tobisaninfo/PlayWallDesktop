package de.tobias.playpad.pad.content;

import de.tobias.playpad.pad.mediapath.MediaPath;
import javafx.beans.property.IntegerProperty;

public interface Playlistable {

	String SHUFFLE_SETTINGS_KEY = "shuffle";
	String AUTO_NEXT_SETTINGS_KEY = "autoNext";

	int getCurrentPlayingMediaIndex();

	IntegerProperty currentPlayingMediaIndexProperty();

	boolean hasNext();

	void next();

	boolean isLoaded(MediaPath mediaPath);
}
