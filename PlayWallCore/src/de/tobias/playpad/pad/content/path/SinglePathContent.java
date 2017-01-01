package de.tobias.playpad.pad.content.path;

import java.nio.file.Path;

/**
 * A PadContent should implement this interface, if the content consists of one file. If this interface is implemented, the program can easily obtain the media path.
 *
 * @author tobias
 *
 */
public interface SinglePathContent {

	/**
	 * Get the media path of the content.
	 * @return media path
	 */
	Path getPath();
}
