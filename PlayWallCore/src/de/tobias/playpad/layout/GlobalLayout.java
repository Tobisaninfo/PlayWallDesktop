package de.tobias.playpad.layout;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import de.tobias.playpad.pad.Warning;
import de.tobias.playpad.pad.view.IPadViewController;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.stage.Stage;

public interface GlobalLayout {

	public void applyCss(Stage stage);

	public void applyCssMainView(IMainViewController controller, Stage stage, Project project);

	/*
	 * Root: <CartLayout type=Classic>
	 */
	public void load(Element rootElement);

	/*
	 * Root: <CartLayout type=Classic>
	 */
	public void save(Element rootElement);

	public double getMinWidth(int column);

	public double getMinHeight(int rows);

	public double getPadWidth();

	public double getPadHeight();

	/**
	 * Wird in einem neuen Thread aufgerufen
	 * 
	 * @param controller
	 * @param warning
	 */
	public void handleWarning(IPadViewController controller, Warning warning);

	public default void stopWarning(IPadViewController controller) {}

	public void reset();

	// Utils
	public static void saveGlobal(HashMap<String, GlobalLayout> layouts, Path path) throws UnsupportedEncodingException, IOException {
		Document document = DocumentHelper.createDocument();
		Element rootElement = document.addElement("Layouts");
		for (String key : layouts.keySet()) {
			Element layoutElement = rootElement.addElement("Layout");
			layoutElement.addAttribute("type", key);

			GlobalLayout layout = layouts.get(key);
			layout.save(layoutElement);
		}

		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}

	public static HashMap<String, GlobalLayout> loadGlobalLayout(Path path) throws DocumentException, IOException {
		HashMap<String, GlobalLayout> layouts = new HashMap<>();

		if (Files.exists(path)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(path));
			Element root = document.getRootElement();

			for (Object layoutObj : root.elements("Layout")) {
				Element layoutElement = (Element) layoutObj;
				String type = layoutElement.attributeValue("type");
				GlobalLayout layout = LayoutRegistry.getLayout(type).newGlobalLayout();
				layout.load(layoutElement);
				layouts.put(type, layout);
			}
		}

		return layouts;
	}
}
