package de.tobias.playpad.pad.content.path;

import java.nio.file.Path;
import java.util.List;

/**
 * A PadContent should implement this interface, if the content consists of multiple files. If this interface is implemented, the program can easily obtain the media paths.
 *
 * @author tobias
 */
public interface MultiPathContent {

	/**
	 * Get a list of the used media files.
	 *
	 * @return media files
	 */
	List<Path> getPaths();

	/**
	 * Clears all media path from the content.
	 */
	void clearPaths();
}
