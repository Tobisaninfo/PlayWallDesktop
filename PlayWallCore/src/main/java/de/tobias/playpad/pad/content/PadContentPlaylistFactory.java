package de.tobias.playpad.pad.content;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.mediapath.MediaPath;
import javafx.scene.Node;

public interface PadContentPlaylistFactory {

	Node getCustomPlaylistItemView(Pad pad, MediaPath mediaPath);
}
