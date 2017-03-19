package de.tobias.playpad.tigger;

import org.dom4j.Element;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.util.Duration;

public abstract class TriggerItem {

	protected Duration durationFromPoint;

	protected transient Duration performedAt;

	public TriggerItem() {
		durationFromPoint = Duration.ZERO;
		performedAt = null;
	}

	public Duration getDurationFromPoint() {
		return durationFromPoint;
	}

	public void setDurationFromPoint(Duration durationFromPoint) {
		this.durationFromPoint = durationFromPoint;
	}

	public Duration getPerformedAt() {
		return performedAt;
	}

	public void setPerformedAt(Duration performedAt) {
		this.performedAt = performedAt;
	}

	/**
	 * Get an identification name
	 * 
	 * @return name
	 */
	public abstract String getType();

	public abstract void performAction(Pad pad, Project project, IMainViewController controller, Profile profile);

	private static final String DURATION_ATTR = "duration";

	/**
	 * You must call super.load
	 * 
	 * @param element
	 *            XML Element
	 */
	public void load(Element element) {
		if (element.attributeValue(DURATION_ATTR) != null) {
			durationFromPoint = Duration.millis(Double.valueOf(element.attributeValue(DURATION_ATTR)));
		}
	}

	/**
	 * You must call super.save
	 * 
	 * @param element
	 *            XMl Element
	 */
	public void save(Element element) {
		element.addAttribute(DURATION_ATTR, String.valueOf(durationFromPoint.toMillis()));
	}

}
