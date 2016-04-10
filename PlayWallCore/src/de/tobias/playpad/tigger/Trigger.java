package de.tobias.playpad.tigger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dom4j.Element;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.util.Duration;

public class Trigger {

	private TriggerPoint triggerPoint;

	private List<TriggerItem> items;

	public Trigger() {
		triggerPoint = TriggerPoint.START;
		items = new ArrayList<>();
	}

	public Trigger(TriggerPoint point) {
		triggerPoint = point;
		items = new ArrayList<>();
	}

	public List<TriggerItem> getItems() {
		return Collections.unmodifiableList(items);
	}

	public void addItem(TriggerItem item) {
		items.add(item);
	}

	public void removeItem(TriggerItem item) {
		items.remove(item);
	}

	public TriggerPoint getTriggerPoint() {
		return triggerPoint;
	}

	public void setTriggerPoint(TriggerPoint triggerPoint) {
		this.triggerPoint = triggerPoint;
	}

	private static final String TYPE_ATTR = "type";
	private static final String ITEM_ELEMENT = "Item";
	private static final String POINT_ATTR = "point";

	public void load(Element element) {
		try {
			triggerPoint = TriggerPoint.valueOf(element.attributeValue(POINT_ATTR));
		} catch (Exception e) {}

		for (Object itemObj : element.elements(ITEM_ELEMENT)) {
			if (itemObj instanceof Element) {
				Element itemElement = (Element) itemObj;
				String type = itemElement.attributeValue(TYPE_ATTR);
				TriggerItemConnect connect = TriggerRegistry.getTriggerConnect(type);
				TriggerItem item = connect.newInstance(this);
				item.load(itemElement);
				items.add(item);
			}
		}
	}

	public void save(Element element) {
		element.addAttribute(POINT_ATTR, triggerPoint.name());

		for (TriggerItem item : items) {
			Element itemElement = element.addElement(ITEM_ELEMENT);
			itemElement.addAttribute(TYPE_ATTR, item.getType());
			item.save(itemElement);
		}
	}

	@Override
	public String toString() {
		return triggerPoint.name() + " (" + items.size() + ")";
	}

	public void handle(Pad pad, Duration duration, Project project, IMainViewController mainViewController, Profile currentProfile) {
		for (TriggerItem item : items) {

			// Reset Time - Damit ein Trigger nicht mehrmals aufgeführt wird (TriggerPoint = 5s -> wird sonst immer ab 5s ausgeführt, nun
			// aber nur einmal, bis die aktuelle Zeit wieder <5s ist, dann wird es zurückgesetzt)
			if (triggerPoint == TriggerPoint.START && item.getPerformedAt().greaterThan(duration)) {
				item.setPerformedAt(Duration.ZERO);
			} else if (triggerPoint == TriggerPoint.EOF_STOP && item.getPerformedAt().lessThan(duration)) {
				item.setPerformedAt(Duration.ZERO);
			}

			if (triggerPoint == TriggerPoint.START
					&& (item.getDurationFromPoint().lessThan(duration) || item.getDurationFromPoint().equals(duration))
					&& item.getPerformedAt().equals(Duration.ZERO)) {
				item.performAction(pad, project, mainViewController, currentProfile);
				item.setPerformedAt(duration);
			} else if (triggerPoint == TriggerPoint.EOF_STOP
					&& (item.getDurationFromPoint().greaterThan(duration) || item.getDurationFromPoint().equals(duration))
					&& item.getPerformedAt().equals(Duration.ZERO)) {
				item.performAction(pad, project, mainViewController, currentProfile);
				item.setPerformedAt(duration);
			}
		}
	}
}
