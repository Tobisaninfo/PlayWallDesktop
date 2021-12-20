package de.tobias.playpad.pad.content;

import de.tobias.playpad.pad.Pad;

public interface PlaylistListener {

	void onPlaylistStart(Pad pad);

	void onPlaylistItemStart(Pad pad);

	void onPlaylistItemEnd(Pad pad);

	void onPlaylistEnd(Pad pad);
}
