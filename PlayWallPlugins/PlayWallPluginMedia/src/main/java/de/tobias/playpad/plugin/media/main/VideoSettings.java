package de.tobias.playpad.plugin.media.main;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class VideoSettings {

	private int screenId = 0;
	private boolean fullScreen = true;
	private boolean openAtLaunch = false;

	public int getScreenId() {
		return screenId;
	}

	public boolean isFullScreen() {
		return fullScreen;
	}

	public boolean isOpenAtLaunch() {
		return openAtLaunch;
	}

	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
	}

	public void setOpenAtLaunch(boolean openAtLaunch) {
		this.openAtLaunch = openAtLaunch;
	}

	public void setScreenId(int screenId) {
		this.screenId = screenId;
	}

	private static final String OPEN_AT_LAUNCH_ELEMENT = "OpenAtLaunch";
	private static final String FULL_SCREEN_ELEMENT = "FullScreen";
	private static final String SCREEN_ID_ELEMENT = "ScreenID";

	public void load(Path path) throws DocumentException, IOException {
		if (Files.exists(path)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(path));
			Element root = document.getRootElement();
			if (root.element(SCREEN_ID_ELEMENT) != null)
				setScreenId(Integer.valueOf(root.element(SCREEN_ID_ELEMENT).getStringValue()));
			if (root.element(FULL_SCREEN_ELEMENT) != null)
				setFullScreen(Boolean.valueOf(root.element(FULL_SCREEN_ELEMENT).getStringValue()));
			if (root.element(OPEN_AT_LAUNCH_ELEMENT) != null)
				setOpenAtLaunch(Boolean.valueOf(root.element(OPEN_AT_LAUNCH_ELEMENT).getStringValue()));
		}
	}

	public void save(Path path) throws IOException {
		if (Files.notExists(path)) {
			Files.createDirectories(path.getParent());
			Files.createFile(path);
		}
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("Config");

		root.addElement(SCREEN_ID_ELEMENT).addText(String.valueOf(screenId));
		root.addElement(FULL_SCREEN_ELEMENT).addText(String.valueOf(fullScreen));
		root.addElement(OPEN_AT_LAUNCH_ELEMENT).addText(String.valueOf(openAtLaunch));

		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}
}
