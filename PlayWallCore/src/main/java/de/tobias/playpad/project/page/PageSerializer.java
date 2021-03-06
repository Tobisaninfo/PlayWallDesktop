package de.tobias.playpad.project.page;

import de.thecodelabs.storage.xml.XMLDeserializer;
import de.thecodelabs.storage.xml.XMLHandler;
import de.thecodelabs.storage.xml.XMLSerializer;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSerializer;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSerializer;
import org.dom4j.Element;

import java.util.List;
import java.util.UUID;

public class PageSerializer implements XMLSerializer<Page>, XMLDeserializer<Page> {

	private static final String POSITION_ATTR = "id";
	private static final String NAME_ATTR = "name";
	private static final String UUID_ATTR = "uuid";

	private Project project;

	/**
	 * Für Deserialize
	 *
	 * @param project Project Reference
	 */
	public PageSerializer(Project project) {
		this.project = project;
	}

	@Override
	public Page loadElement(Element element) {
		int id = Integer.parseInt(element.attributeValue(POSITION_ATTR));
		String name = element.attributeValue(NAME_ATTR);

		String uuidValue = element.attributeValue(UUID_ATTR);
		UUID uuid;
		if (uuidValue == null) {
			uuid = UUID.randomUUID();
		} else {
			uuid = UUID.fromString(uuidValue);
		}

		XMLHandler<Pad> handler = new XMLHandler<>(element);
		List<Pad> pads = handler.loadElements(ProjectSerializer.PAD_ELEMENT, new PadSerializer(project));

		Page page = new Page(uuid, id, name, project);

		// Set page reference to pads
		for (Pad pad : pads) {
			pad.setPage(page);
			page.setPad(pad.getPosition(), pad);
		}

		return page;
	}

	@Override
	public void saveElement(Element newElement, Page data) {
		newElement.addAttribute(UUID_ATTR, data.getId().toString());
		newElement.addAttribute(POSITION_ATTR, String.valueOf(data.getPosition()));
		newElement.addAttribute(NAME_ATTR, data.getName());

		XMLHandler<Pad> handler = new XMLHandler<>(newElement);
		handler.saveElements(ProjectSerializer.PAD_ELEMENT, data.getPads(), new PadSerializer(project));
	}
}
