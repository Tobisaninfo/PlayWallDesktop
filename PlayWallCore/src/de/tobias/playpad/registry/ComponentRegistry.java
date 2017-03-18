package de.tobias.playpad.registry;

import de.tobias.playpad.plugin.Module;
import de.tobias.utils.ui.icon.FontIconType;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Eine Implementierung für eine Registry.
 *
 * @param <C> Componentent
 * @author tobias
 * @since 5.1.0
 */
public class ComponentRegistry<C extends Component> implements Registry<C> {

	private HashMap<String, Item<C>> components;

	private String name;

	public ComponentRegistry(String name) {
		this.components = new HashMap<>();
		this.name = name;
	}

	@Override
	public void registerComponent(C factory, Module module) throws IllegalArgumentException {
		if (components.containsKey(factory.getType())) {
			throw new IllegalArgumentException("A components already exists with this id: " + factory.getType());
		}

		Item<C> item = new Item<>();
		item.content = factory;
		item.module = module;

		components.put(factory.getType(), item);
		System.out.println("Registered: " + name + "#" + factory.getType());
	}

	@Override
	public C getFactory(String id) throws NoSuchComponentException {
		if (!components.containsKey(id)) {
			throw new NoSuchComponentException(id);
		}
		return components.get(id).content;
	}

	@Override
	public C getFactory(Class<?> clazz) throws NoSuchComponentException {
		for (Item<C> item : components.values()) {
			if (item.content.getClass().equals(clazz)) {
				return item.content;
			}
		}
		throw new NoSuchComponentException(clazz.getName());
	}

	@Override
	public Set<String> getTypes() {
		return components.keySet();
	}

	@Override
	public Collection<C> getComponents() {
		// Maps internal structure to Content List
		return components.values().stream().map(a -> a.content).collect(Collectors.toList());
	}

	@Override
	public void loadComponentsFromFile(URL url, ClassLoader loader, Module module, ResourceBundle resourceBundle)
			throws DocumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		if (url == null) {
			throw new IllegalArgumentException("URL is null");
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
				Constructor<C> constructor = clazz.getConstructor(String.class);
				C factory = constructor.newInstance(type);

				// setup Displayable
				if (element.attributeValue("name") != null) {
					String name = element.attributeValue("name");
					String localizedName = resourceBundle.getString(name);
					factory.setName(localizedName);
				}

				if (element.attributeValue("icon") != null && element.attributeValue("class") != null && element.attributeValue("size") != null) {
					String icon = element.attributeValue("icon");
					Class iconClass = Class.forName(element.attributeValue("class"));
					int size = Integer.valueOf(element.attributeValue("size"));
					Object iconObj = Enum.valueOf(iconClass, icon);
					if (iconObj instanceof FontIconType) {
						FontIconType iconType = (FontIconType) iconObj;
						factory.setGraphics(iconType, size);
					}
				}

				registerComponent(factory, module);
			}
		}
	}

	@Override
	public Module getModule(String id) {
		return components.get(id).module;
	}


}
