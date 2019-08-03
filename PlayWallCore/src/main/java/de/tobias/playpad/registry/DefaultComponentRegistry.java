package de.tobias.playpad.registry;

import de.thecodelabs.utils.ui.icon.FontIconType;
import de.tobias.playpad.plugin.Module;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ResourceBundle;

public class DefaultComponentRegistry<F extends Component> extends ComponentRegistry<F> implements DefaultRegistry<F> {

	private F defaultValue;

	public DefaultComponentRegistry(String name) {
		super(name);
	}

	@Override
	public F getDefault() {
		return defaultValue;
	}

	@Override
	public String getDefaultID() {
		for (String type : getTypes()) {
			try {
				if (getFactory(type).equals(defaultValue)) {
					return type;
				}
			} catch (NoSuchComponentException e) {
				// Exception will never been thrown, because all elements (getTypes()) exists. Otherwise something is totally wrong.
			}
		}
		return null;
	}

	@Override
	public void setDefault(F component) {
		this.defaultValue = component;
	}

	@Override
	public void setDefaultID(String id) throws NoSuchComponentException {
		setDefault(getFactory(id));
	}

	@Override
	public void setDefaultID(Class<?> clazz) throws NoSuchComponentException {
		setDefault(getFactory(clazz));
	}

	@Override
	public void loadComponentsFromFile(URL url, ClassLoader loader, Module module, ResourceBundle resourceBundle)
			throws RuntimeException {
		if (url == null) {
			throw new IllegalArgumentException("URL is null");
		}
		try {
			SAXReader reader = new SAXReader();
			Document document = reader.read(url);

			Element rootElement = document.getRootElement();
			for (Object obj : rootElement.elements("Component")) {
				if (obj instanceof Element) {
					Element element = (Element) obj;
					String type = element.attributeValue("id");

					// Find the class of the type
					@SuppressWarnings("unchecked") Class<F> clazz = (Class<F>) loader.loadClass(element.getStringValue().trim());
					Constructor<F> constructor = clazz.getConstructor(String.class);
					F factory = constructor.newInstance(type);

					registerComponent(factory, module);

					if (element.attributeValue("default") != null) {
						String defaultValue = element.attributeValue("default");
						if (defaultValue.equals("true")) {
							setDefault(factory);
						}
					}

					// setup Displayable
					if (element.attributeValue("name") != null) {
						String name = element.attributeValue("name");
						String localizedName = resourceBundle.getString(name);
						factory.setName(localizedName);
					}

					if (element.attributeValue("icon") != null && element.attributeValue("class") != null && element.attributeValue("size") != null) {
						String icon = element.attributeValue("icon");
						Class iconClass = Class.forName(element.attributeValue("class"));
						int size = Integer.parseInt(element.attributeValue("size"));
						Object iconObj = Enum.valueOf(iconClass, icon);
						if (iconObj instanceof FontIconType) {
							FontIconType iconType = (FontIconType) iconObj;
							factory.setGraphics(iconType, size);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
