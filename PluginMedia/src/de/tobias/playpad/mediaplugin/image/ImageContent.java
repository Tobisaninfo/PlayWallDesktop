package de.tobias.playpad.mediaplugin.image;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.dom4j.Element;

import de.tobias.playpad.mediaplugin.main.impl.MediaPluginImpl;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.project.ProjectExporter;
import de.tobias.utils.util.ZipFile;
import javafx.application.Platform;

public class ImageContent extends PadContent {

	private final String type;

	private Path path;

	public ImageContent(String type, Pad pad) {
		super(pad);
		this.type = type;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	@Override
	public void handlePath(Path path) throws IOException {
		unloadMedia();
		setPath(getRealPath(path));
		loadMedia();
	}

	@Override
	public void updateVolume() {}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void play() {
		MediaPluginImpl.getInstance().getVideoViewController().setImage(path.toUri().toString(), getPad());
	}

	@Override
	public boolean stop() {
		MediaPluginImpl.getInstance().getVideoViewController().setImage(null, null);
		return true;
	}

	@Override
	public boolean isPadLoaded() {
		return path != null;
	}

	@Override
	public void loadMedia() {
		if (Files.exists(path)) {
			getPad().setStatus(PadStatus.READY);
		} else {
			// getPad().throwException(path, new FileNotFoundException()); TODO Error Handling User
		}
	}

	@Override
	public void unloadMedia() {
		// First Stop the pad (if playing)
		getPad().setStatus(PadStatus.STOP);

		Platform.runLater(() ->
		{
			if (getPad() != null) {
				getPad().setStatus(PadStatus.EMPTY);
			}
		});
	}

	@Override
	public void load(Element element) {
		path = Paths.get(element.getStringValue());
	}

	@Override
	public void save(Element element) {
		element.addText(path.toString());
	}

	@Override
	public void importMedia(Path mediaFolder, ZipFile zip, Element element) {
		String fileName = Paths.get(element.getStringValue()).getFileName().toString();
		Path mediaFile = Paths.get(ProjectExporter.mediaFolder, fileName);

		Path desFile = mediaFolder.resolve(fileName);

		try {
			if (Files.notExists(desFile.getParent())) {
				Files.createDirectories(desFile.getParent());
			}

			if (Files.notExists(desFile))
				zip.getFile(mediaFile, desFile);

			element.setText(desFile.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void exportMedia(ZipFile zip, Element element) {
		Path desPath = Paths.get(ProjectExporter.mediaFolder, path.getFileName().toString());
		try {
			if (Files.notExists(desPath.getParent())) {
				Files.createDirectories(desPath.getParent());
			}
			zip.addFile(path, desPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public PadContent clone() throws CloneNotSupportedException {
		ImageContent clone = (ImageContent) super.clone();
		clone.path = Paths.get(path.toUri());
		clone.loadMedia();
		return clone;
	}

}