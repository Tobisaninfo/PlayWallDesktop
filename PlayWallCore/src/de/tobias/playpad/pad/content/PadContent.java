package de.tobias.playpad.pad.content;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.dom4j.Element;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.utils.util.ZipFile;

/**
 * Verarbeitet den Inhalt eines Pads. Die Einstellungen und der Status ist in Pad ausgelagert.
 * 
 * @author tobias
 *
 * @version 5.1.0
 * @see Pad
 */
public abstract class PadContent implements Cloneable {

	// Refrence
	private Pad pad;

	public PadContent(Pad pad) {
		this.pad = pad;
	}

	public Pad getPad() {
		return pad;
	}

	/**
	 * Never use this. only for cloning
	 * @param pad
	 */
	public void setPad(Pad pad) {
		this.pad = pad;
	}

	public abstract String getType();

	public abstract void play();

	public abstract boolean stop();

	public abstract boolean isPadLoaded();

	/**
	 * Verarbeitet eien neuen Path für das Pad.
	 * 
	 * @param path
	 *            path
	 * @throws NoSuchComponentException
	 *             Wird geworfen, wenn ein Pad eine Componenten nicht laden kann. Beispiel bei Audio das richtige Soundsystem
	 * @throws IOException
	 *             IO Fehler
	 */
	public abstract void handlePath(Path path) throws NoSuchComponentException, IOException;

	/**
	 * Lädt die Medien, sodass sie auf abruf verfügbar sind.
	 */
	public abstract void loadMedia();

	/**
	 * Entfernt die Medien aus dem Speicher (lässt diese aber im Pad).
	 */
	public abstract void unloadMedia();

	public abstract void updateVolume();

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
	public abstract void importMedia(Path mediaFolder, ZipFile zipfs, Element element);

	public abstract void exportMedia(ZipFile zip, Element element);

	/**
	 * Gibt den richtigen Pfad einer Datei zurück, basierend auf den Einstellungen.
	 * 
	 * @param orrginal
	 *            orginal path
	 * @return new path
	 * @throws IOException
	 *             IO Fehler
	 * @since 5.1.0
	 */
	public Path getRealPath(Path orginal) throws IOException {
		ProjectSettings settings = getPad().getProject().getSettings();
		if (settings.isUseMediaPath()) {
			Path mediaFolder = settings.getMediaPath();
			Path newPath = mediaFolder.resolve(orginal.getFileName());

			if (Files.notExists(mediaFolder)) {
				Files.createDirectories(mediaFolder);
			}

			Files.copy(orginal, newPath, StandardCopyOption.REPLACE_EXISTING);
			return newPath;
		}
		return orginal;
	}

	@Override
	public PadContent clone() throws CloneNotSupportedException {
		PadContent clone = (PadContent) super.clone();
		return clone;
	}

}