package de.tobias.playpad.action;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import de.tobias.playpad.settings.Profile;

public class MappingList extends ArrayList<Mapping> {

	private static final long serialVersionUID = 1L;

	private UUID activeMapping;

	public MappingList(Profile profile) {
		add(new Mapping(true, profile));
	}

	public Mapping getActiveMapping() {
		for (Mapping mapping : this) {
			if (mapping.getUuid().equals(activeMapping)) {
				return mapping;
			}
		}
		if (size() > 0) {
			activeMapping = get(0).getUuid();
			return get(0);
		} else {
			return null;
		}
	}

	public void setActiveMapping(Mapping mapping) {
		activeMapping = mapping.getUuid();
	}

	private static final String LIST = "List";
	private static final String MAPPING = "Mapping";
	private static final String ACTIVE_ATTR = "active";

	public static MappingList load(Path path, Profile profile) throws DocumentException, IOException {
		MappingList mappings = new MappingList(profile);
		mappings.clear(); // Clear the default mapping

		if (Files.exists(path)) {
			SAXReader reader = new SAXReader();
			Document document = reader.read(Files.newInputStream(path));

			Element rootElement = document.getRootElement();
			if (rootElement.attributeValue(ACTIVE_ATTR) != null) {
				mappings.activeMapping = UUID.fromString(rootElement.attributeValue(ACTIVE_ATTR));
			}

			for (Object mappingObj : rootElement.elements(MAPPING)) {
				if (mappingObj instanceof Element) {
					Element mappingElement = (Element) mappingObj;

					Mapping mapping = new Mapping(false, profile);
					mapping.load(mappingElement, profile);

					mappings.add(mapping);
				}
			}
		}

		// Init mappings, if non exists
		if (mappings.size() == 0) {
			mappings.add(new Mapping(true, profile));
		}

		return mappings;
	}

	public void save(Path path) throws UnsupportedEncodingException, IOException {
		Document document = DocumentHelper.createDocument();

		Element rootElement = document.addElement(LIST);
		if (activeMapping != null)
			rootElement.addAttribute(ACTIVE_ATTR, activeMapping.toString());

		for (Mapping mapping : this) {
			Element mappingElement = rootElement.addElement(MAPPING);
			mapping.save(mappingElement);
		}

		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}

	public static Mapping importMappingPreset(Path path, Profile profile) throws DocumentException, IOException {
		Mapping mapping = new Mapping(false, profile);

		SAXReader reader = new SAXReader();
		Document document = reader.read(Files.newInputStream(path));
		Element rootElement = document.getRootElement();

		mapping.load(rootElement, profile);
		mapping.setUuid(UUID.randomUUID());

		return mapping;
	}

	public static void exportMidiPreset(Path path, Mapping preset) throws IOException {
		Document docoment = DocumentHelper.createDocument();
		Element rootElement = docoment.addElement(MAPPING);
		preset.save(rootElement);

		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(docoment);
		writer.close();
	}
}
