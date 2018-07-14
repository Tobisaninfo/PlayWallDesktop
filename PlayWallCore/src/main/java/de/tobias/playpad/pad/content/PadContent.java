package de.tobias.playpad.pad.content;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.mediapath.MediaPath;

/**
 * Verarbeitet den Inhalt eines Pads. Die Einstellungen und der Status ist in Pad ausgelagert.
 *
 * @author tobias
 * @version 5.1.0
 * @see Pad
 */
public abstract class PadContent implements Cloneable {

	// reference
	private Pad pad;

	public PadContent(Pad pad) {
		this.pad = pad;
	}

	public Pad getPad() {
		return pad;
	}

	/**
	 * Never use this. only for cloning
	 *
	 * @param pad cloned pad
	 */
	public void setPad(Pad pad) {
		this.pad = pad;
	}

	public abstract String getType();

	public abstract void play();

	public abstract boolean stop();

	public abstract boolean isPadLoaded();

	/**
	 * Load media files.
	 */
	public abstract void loadMedia();

	/**
	 * Load media file.
	 *
	 * @param mediaPath specify media path
	 */
	public abstract void loadMedia(MediaPath mediaPath);

	/**
	 * Unload media files.
	 */
	public abstract void unloadMedia();

	/**
	 * Unload media file.
	 *
	 * @param mediaPath specify media path
	 */
	public abstract void unloadMedia(MediaPath mediaPath);

	public abstract void updateVolume();

	@Override
	protected void finalize() throws Throwable {
		unloadMedia();
		this.pad = null;
	}

	@Override
	public PadContent clone() throws CloneNotSupportedException {
		return (PadContent) super.clone();
	}

}