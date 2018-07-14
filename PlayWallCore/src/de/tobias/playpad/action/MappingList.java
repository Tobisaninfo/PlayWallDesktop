package de.tobias.playpad.action;

import de.tobias.playpad.profile.Profile;
import de.tobias.utils.xml.XMLHandler;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// COMMENT MappingList
public class MappingList extends ArrayList<Mapping> {

	private static final long serialVersionUID = 1L;

	private UUID activeMapping;
	private WeakReference<Profile> profile;

	public MappingList(Profile profile) {
		add(new Mapping(true));

		this.profile = new WeakReference<Profile>(profile);
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

			// Load Mappings
			XMLHandler<Mapping> handler = new XMLHandler<>(rootElement);
			List<Mapping> loadMappings = handler.loadElements(MAPPING, new MappingSerializer(profile));
			loadMappings.forEach(mapping ->
			{
				mapping.initActionType(profile); // Update Actions, damit alle da sind und keine fehlt (falls eine gelöscht wurde
													// auf der Datei)
				mapping.updateDisplayProperty();
				mappings.add(mapping);
			});
		}

		// Init mappings, if non exists
		if (mappings.size() == 0) {
			mappings.add(new Mapping(true));
		}

		return mappings;
	}

	public void save(Path path) throws IOException {
		Document document = DocumentHelper.createDocument();

		Element rootElement = document.addElement(LIST);
		if (activeMapping != null)
			rootElement.addAttribute(ACTIVE_ATTR, activeMapping.toString());

		if (Files.notExists(path)) {
			Files.createDirectories(path.getParent());
			Files.createFile(path);
		}

		XMLHandler<Mapping> handler = new XMLHandler<>(rootElement);
		handler.saveElements(MAPPING, this, new MappingSerializer(profile.get()));

		if (Files.notExists(path)) {
			Files.createDirectories(path.getParent());
			Files.createFile(path);
		}

		XMLHandler.save(path, document);
	}

	public static Mapping importMappingPreset(Path path, Profile profile) throws DocumentException, IOException {
		Mapping mapping = new Mapping(false);

		SAXReader reader = new SAXReader();
		Document document = reader.read(Files.newInputStream(path));
		Element rootElement = document.getRootElement();

		MappingSerializer mappingSerializer = new MappingSerializer(profile);
		mapping = mappingSerializer.loadElement(rootElement);
		mapping.setUuid(UUID.randomUUID());

		return mapping;
	}

	public static void exportMidiPreset(Path path, Mapping preset) throws IOException {
		Document docoment = DocumentHelper.createDocument();
		Element rootElement = docoment.addElement(MAPPING);

		MappingSerializer mappingSerializer = new MappingSerializer(null);
		mappingSerializer.saveElement(rootElement, preset);

		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(docoment);
		writer.close();
	}

	public boolean containsName(String name) {
		return stream().anyMatch(i -> i.getName().equals(name));
	}
}
