package de.tobias.playpad.project.page;

import java.util.List;

import org.dom4j.Element;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSerializer;
import de.tobias.playpad.project.Project;
import de.tobias.utils.xml.XMLDeserializer;
import de.tobias.utils.xml.XMLHandler;
import de.tobias.utils.xml.XMLSerializer;

public class PageSerializer implements XMLSerializer<Page>, XMLDeserializer<Page> {

	private static final String ID_ATTR = "id";
	private static final String NAME_ATTR = "name";

	private Project project;

	/**
	 * Für Deserialize
	 * 
	 * @param project
	 *            Project Reference
	 */
	public PageSerializer(Project project) {
		this.project = project;
	}

	@Override
	public Page loadElement(Element element) {
		int id = Integer.valueOf(element.attributeValue(ID_ATTR));
		String name = element.attributeValue(NAME_ATTR);

		XMLHandler<Pad> handler = new XMLHandler<>(element);
		List<Pad> pads = handler.loadElements(Project.PAD_ELEMENT, new PadSerializer(project));

		Page page = new Page(id, name, project);
		for (Pad pad : pads) {
			pad.setPage(id);
			page.setPad(pad.getIndex(), pad);
		}

		return page;
	}

	@Override
	public void saveElement(Element newElement, Page data) {
		newElement.addAttribute(ID_ATTR, String.valueOf(data.getId()));
		newElement.addAttribute(NAME_ATTR, data.getName());

		XMLHandler<Pad> handler = new XMLHandler<>(newElement);
		handler.saveElements(Project.PAD_ELEMENT, data.getPads(), new PadSerializer(project));
	}
}
