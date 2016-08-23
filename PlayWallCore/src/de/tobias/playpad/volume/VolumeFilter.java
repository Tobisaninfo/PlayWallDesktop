package de.tobias.playpad.volume;

import de.tobias.playpad.pad.Pad;

/**
 * Interface, um das Volume eines Pad zu beeinflussen. Es muss dem VolumeManager hinzugefügt werden.
 * 
 * @author tobias
 *
 * @sinve 6.0.0
 * 
 * @see VolumeManager#addFilter(VolumeFilter)
 */
@FunctionalInterface
public interface VolumeFilter {

	double getVolume(Pad pad);

}
