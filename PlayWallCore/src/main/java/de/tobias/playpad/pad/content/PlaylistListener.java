package de.tobias.playpad.pad.content;

import de.tobias.playpad.pad.Pad;

public interface PlaylistListener {

	void onNextItem(Pad pad, int next, int total);
}
