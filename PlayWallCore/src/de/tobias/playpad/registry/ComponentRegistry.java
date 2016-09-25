package de.tobias.playpad.registry;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import de.tobias.playpad.plugin.Module;

/**
 * Eine Implementierung für eine Registry.
 * 
 * @author tobias
 *
 * @param <C>
 *            Componentent
 * 
 * @since 5.1.0
 */
public class ComponentRegistry<C> implements Registry<C> {

	private HashMap<String, C> components;
	// Zu einem Component die zugehörigen Meta Daten (das Modul)
	private HashMap<String, Module> modules;
	private String name;

	public ComponentRegistry(String name) {
		this.components = new HashMap<>();
		this.modules = new HashMap<>();
		this.name = name;
	}

	@Override
	public void registerComponent(C component, String id, Module module) throws IllegalArgumentException {
		if (components.containsKey(id)) {
			throw new IllegalArgumentException("A components already exists with this id: " + id);
		}
		components.put(id, component);
		modules.put(id, module);
		System.out.println("Registered: " + name + "#" + id);
	}

	@Override
	public C getComponent(String id) throws NoSuchComponentException {
		if (!components.containsKey(id)) {
			throw new NoSuchComponentException(id);
		}
		return components.get(id);
	}

	@Override
	public Set<String> getTypes() {
		return components.keySet();
	}

	@Override
	public Collection<C> getComponents() {
		return components.values();
	}

	@Override
	public void loadComponentsFromFile(URL url, ClassLoader loader, Module module)
			throws IOException, DocumentException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (url == null) {
			throw new IOException("URL not found: " + url);
		}
		SAXReader reader = new SAXReader();
		Document document = reader.read(url);

		Element rootElement = document.getRootElement();
		for (Object obj : rootElement.elements("Component")) {
			if (obj instanceof Element) {
				Element element = (Element) obj;
				String type = element.attributeValue("id");

				// Find the class of the type
				@SuppressWarnings("unchecked") Class<C> clazz = (Class<C>) loader.loadClass(element.getStringValue());
				C component = clazz.newInstance();

				registerComponent(component, type, module);
			}
		}
	}

	@Override
	public Module getModule(String id) {
		return modules.get(id);
	}
}
