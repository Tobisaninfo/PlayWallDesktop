package de.tobias.playpad.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.dom4j.Element;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.action.mapper.MapperConnect;
import de.tobias.playpad.action.mapper.MapperConnectFeedbackable;
import de.tobias.playpad.action.mapper.MapperRegistry;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Mapping implements Cloneable, ActionDisplayable {

	private String name;
	private UUID uuid;
	private HashMap<Action, List<Mapper>> mapping;

	public Mapping(boolean init, Profile profile) {
		mapping = new HashMap<>();
		if (init) {
			initActionType(profile);
			name = "Default";
			uuid = UUID.randomUUID();
		}
		updateDisplayProperty();
	}

	List<Mapper> getMapperForAction(Action action) {
		if (mapping.containsKey(action)) {
			return mapping.get(action);
		} else {
			return null;
		}
	}

	public String getName() {
		return name;
	}

	public final UUID getUuid() {
		return uuid;
	}

	public void setName(String name) {
		this.name = name;
		updateDisplayProperty();
	}

	public final void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public Set<Action> getActions() {
		return mapping.keySet();
	}

	@SuppressWarnings("unchecked")
	public <T extends Action> List<T> getActions(String type) {
		return (List<T>) getActionsOfType(type);
	}

	public List<Action> getActionsOfType(String type) {
		return mapping.keySet().stream().filter(i -> i.getType().equals(type)).collect(Collectors.toList());
	}

	public List<Action> getActionsForMapper(Mapper mapper) {
		List<Action> actions = new ArrayList<>();
		for (Action action : mapping.keySet()) {
			if (mapping.get(action).contains(mapper)) {
				actions.add(action);
			}
		}
		return actions;
	}

	public boolean addActionIfNotContains(Action newAction) {
		for (Action action : mapping.keySet()) {
			if (action.equals(newAction)) {
				return false;
			}
		}
		mapping.put(newAction, new ArrayList<>());
		newAction.setMapping(this);
		return true;
	}

	void addMapperToAction(Mapper mapper, Action to) {
		mapping.get(to).add(mapper);

		// Init Mapper to Action (Set kind of Feedback, depending to the action)
		mapper.setFeedbackType(to.geFeedbackType());
	}

	public void removeAction(Action action) {
		mapping.remove(action);
	}

	void removeMapper(Mapper mapper, Action action) {
		mapping.get(action).remove(mapper);
	}

	public void initActionType(Profile profile) {
		for (String type : ActionRegistery.getTypes()) {
			ActionRegistery.getActionConnect(type).initActionType(this, profile);
		}
	}

	Set<Action> keySet() {
		return mapping.keySet();
	}

	List<Mapper> get(Action action) {
		return mapping.get(action);
	}

	public void initFeedback() {
		for (String mapperType : MapperRegistry.getTypes()) {
			MapperConnect mapper = MapperRegistry.getMapperConnect(mapperType);
			if (mapper instanceof MapperConnectFeedbackable) {
				((MapperConnectFeedbackable) mapper).initFeedbackType();
			}
		}
	}

	public void showFeedback(Project project) {
		IMainViewController controller = PlayPadPlugin.getImplementation().getMainViewController();
		showFeedback(project, controller);
	}

	public void showFeedback(Project project, IMainViewController controller) {
		clearFeedback();

		for (Action action : mapping.keySet()) {
			try {
				action.initFeedback(project, controller);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void clearFeedback() {
		for (String mapperType : MapperRegistry.getTypes()) {
			MapperConnect mapper = MapperRegistry.getMapperConnect(mapperType);
			if (mapper instanceof MapperConnectFeedbackable) {
				((MapperConnectFeedbackable) mapper).clearFeedbackType();
			}
		}
		getActions().forEach(action -> action.clearFeedback());
	}

	private static final String NAME = "name";
	private static final String UUID_NAME = "uuid";
	private static final String ACTION = "Action";
	private static final String ACTION_TYPE = "type";
	private static final String MAPPER = "Mapper";
	private static final String MAPPER_TYPE = "type";

	public void load(Element element, Profile profile) {
		name = element.attributeValue(NAME);
		if (element.attributeValue(UUID_NAME) != null)
			uuid = UUID.fromString(element.attributeValue(UUID_NAME));
		else
			uuid = UUID.randomUUID();

		for (Object obj : element.elements(ACTION)) {
			if (obj instanceof Element) {
				Element actionElement = (Element) obj;
				String tpye = actionElement.attributeValue(ACTION_TYPE);

				Action action = ActionRegistery.getActionConnect(tpye).newInstance();
				action.load(actionElement);

				boolean added = addActionIfNotContains(action);

				if (added) {
					for (Object mapperObj : actionElement.elements(MAPPER)) {
						if (mapperObj instanceof Element) {
							Element mapperElement = (Element) mapperObj;
							String mapperType = mapperElement.attributeValue(MAPPER_TYPE);

							Mapper mapper = MapperRegistry.getMapperConnect(mapperType).createNewMapper();
							mapper.load(mapperElement, action);
							action.addMapper(mapper);
						}
					}
				}
			}
		}

		initActionType(profile); // Update Actions, damit alle da sind und keine fehlt (falls eine gelöscht wurde auf der Datei)
		updateDisplayProperty();
	}

	public void save(Element element) {
		element.addAttribute(NAME, name);
		element.addAttribute(UUID_NAME, uuid.toString());

		for (Action action : mapping.keySet()) {
			Element actionElement = element.addElement(ACTION);
			actionElement.addAttribute(ACTION_TYPE, action.getType());

			action.save(actionElement);

			for (Mapper mapper : mapping.get(action)) {
				Element mapperElement = actionElement.addElement(MAPPER);
				mapperElement.addAttribute(MAPPER_TYPE, mapper.getType());

				mapper.save(mapperElement, action);
			}
		}
	}

	@Override
	public Mapping clone() throws CloneNotSupportedException {
		Mapping clone = (Mapping) super.clone();

		clone.mapping = new HashMap<>();
		for (Action action : mapping.keySet()) {
			Action actionClone = action.cloneAction();
			clone.mapping.put(actionClone, new ArrayList<>());

			for (Mapper mapper : action.getMappers()) {
				Mapper mapperClone = mapper.cloneMapper();
				actionClone.addMapper(mapperClone);
			}
		}

		clone.name = name;
		clone.uuid = UUID.randomUUID();

		clone.displayProperty = new SimpleStringProperty();
		return clone;
	}

	@Override
	public String toString() {
		return name;
	}

	private StringProperty displayProperty = new SimpleStringProperty(toString());

	@Override
	public StringProperty displayProperty() {
		return displayProperty;
	}

	private void updateDisplayProperty() {
		displayProperty.set(toString());
	}
}
