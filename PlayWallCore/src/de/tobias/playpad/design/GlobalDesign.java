package de.tobias.playpad.design;

import java.io.IOException;
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

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.registry.DefaultRegistry;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.stage.Stage;
import javafx.util.Duration;

public interface GlobalDesign {

	void applyCss(Stage stage);

	void applyCssMainView(IMainViewController controller, Stage stage, Project project);

	/*
	 * Root: <CartLayout type=Classic>
	 */
	void load(Element rootElement);

	/*
	 * Root: <CartLayout type=Classic>
	 */
	void save(Element rootElement);

	double getMinWidth(int column);

	double getMinHeight(int rows);

	double getPadWidth();

	double getPadHeight();

	/*
	 * Wird in einem neuen Thread aufgerufen
	 */
	void handleWarning(IPadViewController controller, Duration warning);

	default void stopWarning(IPadViewController controller) {}

	void reset();

	// Utils
	static void saveGlobal(HashMap<String, GlobalDesign> layouts, Path path) throws IOException {
		Document document = DocumentHelper.createDocument();
		Element rootElement = document.addElement("Layouts");
		for (String key : layouts.keySet()) {
			Element layoutElement = rootElement.addElement("Layout");
			layoutElement.addAttribute("type", key);

			GlobalDesign layout = layouts.get(key);
			layout.save(layoutElement);
		}

		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}

	static HashMap<String, GlobalDesign> loadGlobalLayout(Path path) throws DocumentException, IOException {
		HashMap<String, GlobalDesign> layouts = new HashMap<>();

		if (Files.exists(path)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(path));
			Element root = document.getRootElement();

			DefaultRegistry<DesignFactory> layouts2 = PlayPadPlugin.getRegistryCollection().getDesigns();

			for (Object layoutObj : root.elements("Layout")) {
				Element layoutElement = (Element) layoutObj;
				String type = layoutElement.attributeValue("type");

				try {
					GlobalDesign layout = layouts2.getFactory(type).newGlobalDesign();
					layout.load(layoutElement);
					layouts.put(type, layout);
				} catch (NoSuchComponentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return layouts;
	}
}
