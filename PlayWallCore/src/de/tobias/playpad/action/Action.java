package de.tobias.playpad.action;

import java.util.Collections;
import java.util.List;

import org.dom4j.Element;

import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.action.feedback.FeedbackType;
import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.action.mapper.MapperFeedbackable;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.viewcontroller.main.IMainViewController;

/**
 * This class is the base of all program actions, that can be triggered by a mapper. This class handle all input form an mapper.
 * 
 * @author tobias
 * @version 5.0.0
 * 
 * @see Mapper
 */
public abstract class Action implements ActionDisplayable, Cloneable {

	private Mapping mapping;

	/**
	 * Set a reference to the mapping it referce.
	 * 
	 * @param mapping
	 *            mapping
	 */
	protected void setMapping(Mapping mapping) {
		this.mapping = mapping;
	}

	/**
	 * Get an identification name
	 * 
	 * @return name
	 */
	public abstract String getType();

	/**
	 * Handle Input from a Mapper. (Ein seperater Listener händelt das Mapper event und sucht die richtigen Actions dazu raus, die dann über
	 * performAction getriggered werden.)
	 * 
	 * @param type
	 *            input type
	 * @param project
	 *            Current Project
	 * @param mainViewController
	 *            MainViewController Implementation
	 */
	public abstract void performAction(InputType type, Project project, IMainViewController mainViewController);

	/**
	 * Handle Output to Mapper (Feedback)
	 * 
	 * @param message
	 *            Type of Message (Feedback)
	 */
	public void handleFeedback(FeedbackMessage message) {
		for (Mapper mapper : getMappers()) {
			if (mapper instanceof MapperFeedbackable) {
				MapperFeedbackable feedbackable = (MapperFeedbackable) mapper;
				if (feedbackable.supportFeedback()) {
					feedbackable.handleFeedback(message);
				}
			}
		}
	}

	/**
	 * Init first Feedback to Mapper (Feedback)
	 * 
	 * @param project
	 *            refence to the current opened project
	 * @param controller
	 *            reference to the main view controller
	 */
	public abstract void initFeedback(Project project, IMainViewController controller);

	public abstract void clearFeedback();

	/**
	 * Return List of Mappers for this Action. This list is unmodifiable.
	 * 
	 * @return mappers
	 */
	public List<Mapper> getMappers() {
		return Collections.unmodifiableList(mapping.getMapperForAction(this));
	}

	/**
	 * Return List of Mappers for this Action. This list is unmodifiable.
	 * 
	 * @return mappers
	 */
	public List<Mapper> getMapperSorted() {
		List<Mapper> list = mapping.getMapperForAction(this);
		list.sort((Mapper o1, Mapper o2) -> o1.getType().compareTo(o2.getType()));
		return Collections.unmodifiableList(list);
	}

	/**
	 * Add a mapper to an action
	 * 
	 * @param mapper
	 *            new mapper
	 */
	public void addMapper(Mapper mapper) {
		mapping.addMapperToAction(mapper, this);
	}

	/**
	 * Remove a mapper from an action
	 * 
	 * @param mapper
	 *            old mapper
	 */
	public void removeMapper(Mapper mapper) {
		mapping.removeMapper(mapper, this);
	}

	/**
	 * Get the supported feedback type for this action (e.g. SingleFeedback, DoubleFeedback)
	 * 
	 * @return feedback type
	 */
	public abstract FeedbackType geFeedbackType();

	/**
	 * This will be called on an empty action instance, to load content from an xml element.
	 * 
	 * @param root
	 *            Action's root xml element
	 */
	public abstract void load(Element root);

	/**
	 * This method will be call to save the content of an action at savetime. It will be stored in an xml element.
	 * 
	 * @param root
	 *            Action's root xml element
	 */
	public abstract void save(Element root);

	public abstract Action cloneAction() throws CloneNotSupportedException;
}
