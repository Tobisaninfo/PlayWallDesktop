package de.tobias.playpad.registry;

import java.io.IOException;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import de.tobias.playpad.plugin.Module;

public class DefaultComponentRegistry<C> extends ComponentRegistry<C> implements DefaultRegistry<C> {

	private C defaultValue;

	public DefaultComponentRegistry(String name) {
		super(name);
	}

	@Override
	public C getDefault() {
		return defaultValue;
	}

	@Override
	public String getDefaultID() {
		for (String type : getTypes()) {
			try {
				if (getComponent(type).equals(defaultValue)) {
					return type;
				}
			} catch (NoSuchComponentException e) {
				// Exception will never been thrown, because all elements (getTypes()) exists. Otherwise something is totally wrong.
			}
		}
		return null;
	}

	@Override
	public void setDefault(C component) {
		this.defaultValue = component;
	}

	@Override
	public void setDefaultID(String id) throws NoSuchComponentException {
		setDefault(getComponent(id));
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

				if (element.attributeValue("default") != null) {
					String defaultValue = element.attributeValue("default");
					if (defaultValue.equals("true")) {
						setDefault(component);
					}
				}
			}
		}
	}

}
