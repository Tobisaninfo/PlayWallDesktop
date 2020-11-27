package de.tobias.playpad.pad.drag;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.pad.Pad;

import java.io.File;
import java.util.List;

public interface ContentDragOption extends Displayable, Comparable<ContentDragOption> {

	void handleDrop(Pad currentPad, List<File> files);

	@Override
	default int compareTo(ContentDragOption o) {
		return displayProperty().get().compareTo(o.displayProperty().get());
	}
}
