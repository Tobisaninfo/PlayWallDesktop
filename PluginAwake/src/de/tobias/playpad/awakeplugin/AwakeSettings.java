package de.tobias.playpad.awakeplugin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class AwakeSettings {

	public boolean active = false;

	private static final String ACTIVE_ELEMENT = "Active";

	public static AwakeSettings load(Path path) throws DocumentException, IOException {
		AwakeSettings settings = new AwakeSettings();
		SAXReader reader = new SAXReader();
		Document document = reader.read(Files.newInputStream(path));

		Element rootElement = document.getRootElement();
		settings.active = Boolean.valueOf(rootElement.element(ACTIVE_ELEMENT).getStringValue());

		return settings;
	}

	public void save(Path path) throws IOException {
		Document document = DocumentHelper.createDocument();
		Element rootElement = document.addElement("Settings");

		Element activeElement = rootElement.addElement(ACTIVE_ELEMENT);
		activeElement.addText(String.valueOf(active));

		if (Files.notExists(path)) {
			Files.createDirectories(path.getParent());
			Files.createFile(path);
		}
		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}
}
