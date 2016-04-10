package de.tobias.playpad.pad.conntent;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import org.dom4j.Element;

import de.tobias.playpad.pad.Pad;

public abstract class PadContent {

	// Refrence
	private Pad pad;

	public PadContent(Pad pad) {
		this.pad = pad;
	}

	public abstract String getType();

	public abstract void setMasterVolume(double masterVolume);

	public abstract void play();

	public abstract boolean stop();

	public abstract boolean isPadLoaded();

	public Pad getPad() {
		return pad;
	}

	public abstract void handlePath(Path path);

	public abstract void loadMedia();

	public abstract void unloadMedia();

	@Override
	protected void finalize() throws Throwable {
		unloadMedia();
		this.pad = null;
	}

	public abstract void load(Element element);

	public abstract void save(Element element);

	/**
	 * This Methode should copy the media file on drive and update the path refernce in the project save (element)
	 * 
	 * @param mediaFolder
	 *            Destination folder for media
	 * @param zipfs
	 *            Source of Media Files
	 * @param element
	 *            current settings, should update path
	 */
	public abstract void importMedia(Path mediaFolder, FileSystem zipfs, Element element);

	public abstract void exportMedia(FileSystem mediaFolder, Element element);

}