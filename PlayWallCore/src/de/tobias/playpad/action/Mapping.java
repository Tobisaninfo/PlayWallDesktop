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
import de.tobias.playpad.action.mapper.MapperFactory;
import de.tobias.playpad.action.mapper.MapperConnectFeedbackable;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

// COMMENT Mapping
public class Mapping implements Cloneable, ActionDisplayable {

	private String name;
	private UUID uuid;
	private HashMap<Action, List<Mapper>> mapping;

	public Mapping(boolean init) {
		mapping = new HashMap<>();
		if (init) {
			name = "Default";
			uuid = UUID.randomUUID();
		}
		updateDisplayProperty();
	}

	List<Mapper> getMapperForAction(Action action) {
		return mapping.getOrDefault(action, null);
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
	public <T extends Action> List<T> getActions(ActionFactory type) {
		return (List<T>) getActionsOfType(type);
	}

	public List<Action> getActionsOfType(ActionFactory actionFactory) {
		return mapping.keySet().stream().filter(i -> i.getType().equals(actionFactory.getType())).collect(Collectors.toList());
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
		Registry<ActionFactory> actions = PlayPadPlugin.getRegistryCollection().getActions();
		for (ActionFactory component : actions.getComponents()) {
			component.initActionType(this, profile);
		}
	}

	Set<Action> keySet() {
		return mapping.keySet();
	}

	List<Mapper> get(Action action) {
		return mapping.get(action);
	}

	public void initFeedbackType() {
		Registry<MapperFactory> registry = PlayPadPlugin.getRegistryCollection().getMappers();
		for (MapperFactory mapper : registry.getComponents()) {
			if (mapper instanceof MapperConnectFeedbackable) {
				((MapperConnectFeedbackable) mapper).initFeedbackType();
			}
		}
	}

	public void prepareFeedback(Project project) {
		IMainViewController controller = PlayPadPlugin.getImplementation().getMainViewController();

		for (Action action : mapping.keySet()) {
			try {
				action.init(project, controller);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void showFeedback(Project project) {
		IMainViewController controller = PlayPadPlugin.getImplementation().getMainViewController();
		showFeedback(project, controller);
	}

	private void showFeedback(Project project, IMainViewController controller) {
		for (Action action : mapping.keySet()) {
			try {
				action.showFeedback(project, controller);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void clearFeedback() {
		Registry<MapperFactory> registry = PlayPadPlugin.getRegistryCollection().getMappers();
		for (MapperFactory mapper : registry.getComponents()) {
			if (mapper instanceof MapperConnectFeedbackable) {
				((MapperConnectFeedbackable) mapper).clearFeedbackType();
			}
		}

		getActions().forEach(Action::clearFeedback);
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
			actionClone.setMappingRef(clone);
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
