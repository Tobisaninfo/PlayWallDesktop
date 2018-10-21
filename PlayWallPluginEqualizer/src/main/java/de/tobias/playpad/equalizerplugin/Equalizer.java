package de.tobias.playpad.equalizerplugin;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import java.util.HashMap;
import java.util.Set;

public class Equalizer {

	private static Equalizer instance;

	static {
		instance = new Equalizer();
	}

	public static Equalizer getInstance() {
		return instance;
	}

	private HashMap<Integer, DoubleProperty> bands;
	private BooleanProperty enable;

	private Equalizer() {
		bands = new HashMap<>();
		bands.put(19, new SimpleDoubleProperty());
		bands.put(39, new SimpleDoubleProperty());
		bands.put(78, new SimpleDoubleProperty());
		bands.put(156, new SimpleDoubleProperty());
		bands.put(312, new SimpleDoubleProperty());
		bands.put(625, new SimpleDoubleProperty());
		bands.put(1250, new SimpleDoubleProperty());
		bands.put(2500, new SimpleDoubleProperty());
		bands.put(5000, new SimpleDoubleProperty());
		bands.put(10000, new SimpleDoubleProperty());

		enable = new SimpleBooleanProperty(false);
	}

	public Set<Integer> getBands() {
		return bands.keySet();
	}

	public Double getGain(int band) {
		return bands.get(band).get();
	}

	public void setGain(int band, double value) {
		this.bands.get(band).set(value);
	}

	public DoubleProperty gainProperty(int band) {
		return bands.get(band);
	}

	public boolean isEnable() {
		return enable.get();
	}

	public void setEnable(boolean enable) {
		this.enable.set(enable);
	}

	public BooleanProperty enableProperty() {
		return enable;
	}

	private static final String ENABLE_ELEMENT = "enable";
	private static final String WIDTH_ATTR = "width";
	private static final String BAND_ELEMENT = "band";
	private static final String EQUALIZER_ELEMENT = "equalizer";

	public static void save(Path path) throws IOException {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement(EQUALIZER_ELEMENT);
		for (int band : instance.bands.keySet()) {
			root.addElement(BAND_ELEMENT).addAttribute(WIDTH_ATTR, String.valueOf(band)).addText(String.valueOf(instance.bands.get(band).get()));
		}
		root.addElement(ENABLE_ELEMENT).addText(String.valueOf(instance.enable.get()));

		if (Files.notExists(path)) {
			Files.createDirectories(path.getParent());
			Files.createFile(path);
		}

		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}

	public static void load(Path path) throws DocumentException, IOException {
		if (Files.exists(path)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(path));

			Element root = document.getRootElement();
			for (Object bandObj : root.elements(BAND_ELEMENT)) {
				Element element = (Element) bandObj;
				int bandwidth = Integer.valueOf(element.attribute(WIDTH_ATTR).getValue());
				double gain = Double.valueOf(element.getStringValue());
				instance.setGain(bandwidth, gain);
			}
			if (root.element(ENABLE_ELEMENT) != null)
				instance.setEnable(Boolean.valueOf(root.element(ENABLE_ELEMENT).getStringValue()));
		}
	}
}
