package de.tobias.playpad.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.action.feedback.ColorAdjuster;
import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.action.mapper.MapperConnect;
import de.tobias.playpad.action.mapper.MapperConnectFeedbackable;
import de.tobias.playpad.project.v2.ProjectV2;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// COMMENT Mapping
public class Mapping implements Cloneable, ActionDisplayable {

	private String name;
	private UUID uuid;
	private HashMap<Action, List<Mapper>> mapping;

	public Mapping(boolean init, Profile profile) {
		mapping = new HashMap<>();
		if (init) {
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
		newAction.setMappingRef(this);
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
		Registry<ActionConnect> actions = PlayPadPlugin.getRegistryCollection().getActions();
		for (String type : actions.getTypes()) {
			try {
				actions.getComponent(type).initActionType(this, profile); // TODO
			} catch (NoSuchComponentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	Set<Action> keySet() {
		return mapping.keySet();
	}

	List<Mapper> get(Action action) {
		return mapping.get(action);
	}

	public void initFeedback() {
		Registry<MapperConnect> mappers = PlayPadPlugin.getRegistryCollection().getMappers();
		for (String mapperType : mappers.getTypes()) {
			try {
				MapperConnect mapper = mappers.getComponent(mapperType);
				if (mapper instanceof MapperConnectFeedbackable) {
					((MapperConnectFeedbackable) mapper).initFeedbackType();
				}
			} catch (NoSuchComponentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void showFeedback(ProjectV2 project) {
		IMainViewController controller = PlayPadPlugin.getImplementation().getMainViewController();
		showFeedback(project, controller);
	}

	public void showFeedback(ProjectV2 project, IMainViewController controller) {
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
		Registry<MapperConnect> mappers = PlayPadPlugin.getRegistryCollection().getMappers();
		for (String mapperType : mappers.getTypes()) {
			try {
				MapperConnect mapper = mappers.getComponent(mapperType);
				if (mapper instanceof MapperConnectFeedbackable) {
					((MapperConnectFeedbackable) mapper).clearFeedbackType();
				}
			} catch (NoSuchComponentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		getActions().forEach(action -> action.clearFeedback());
	}

	public void adjustPadColorToMapper() {
		ColorAdjuster.applyColorsToMappers();
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

	void updateDisplayProperty() {
		displayProperty.set(toString());
	}
}
