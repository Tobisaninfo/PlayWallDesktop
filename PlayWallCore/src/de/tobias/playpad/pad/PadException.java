package de.tobias.playpad.pad;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import javax.sound.sampled.UnsupportedAudioFileException;

import de.tobias.playpad.registry.NoSuchComponentException;
import javafx.scene.media.MediaException;
import javazoom.jl.decoder.JavaLayerException;

public class PadException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Pad pad;
	private Path path;
	private PadExceptionType type;

	public enum PadExceptionType {
		FILE_NOT_FOUND,
		FILE_FORMAT_NOT_SUPPORTED,
		CONVERT_NOT_SUPPORTED,
		UNKOWN_CONTENT_TYPE,
		UNKOWN;
	}

	public PadException(Pad pad, Path path, Exception ex) {
		this.pad = pad;
		this.path = path;

		if (ex instanceof FileNotFoundException) {
			type = PadExceptionType.FILE_NOT_FOUND;
		} else if (ex instanceof MediaException || ex instanceof UnsupportedAudioFileException) {
			type = PadExceptionType.FILE_FORMAT_NOT_SUPPORTED;
		} else if (ex instanceof JavaLayerException) {
			type = PadExceptionType.CONVERT_NOT_SUPPORTED;
		} else if (ex instanceof IOException) {
			type = PadExceptionType.UNKOWN;
		} else if (ex instanceof NoSuchComponentException) {
			type = PadExceptionType.UNKOWN_CONTENT_TYPE;
		} else if (ex instanceof Exception) {
			type = PadExceptionType.UNKOWN;
		}
	}

	public PadExceptionType getType() {
		return type;
	}

	public Pad getPad() {
		return pad;
	}

	public Path getPath() {
		return path;
	}

	@Override
	public String toString() {
		return "Pad: " + pad + ", Type: " + type;
	}
}
