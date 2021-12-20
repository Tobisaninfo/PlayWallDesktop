package de.tobias.playpad.tigger;

import de.thecodelabs.logger.Logger;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.util.Duration;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	public Trigger copy() {
		Trigger clone = new Trigger(triggerPoint);
		clone.items = new ArrayList<>();

		for (TriggerItem item : items) {
			clone.items.add(item.copy());
		}

		return clone;
	}

	private static final String TYPE_ATTR = "type";
	private static final String ITEM_ELEMENT = "Item";
	private static final String POINT_ATTR = "point";

	public void load(Element element) {
		try {
			triggerPoint = TriggerPoint.valueOf(element.attributeValue(POINT_ATTR));
		} catch (Exception e) {
			throw new IllegalArgumentException("Trigger Point " + element.attributeValue(POINT_ATTR) + " not exists");
		}

		for (Element itemElement : element.elements(ITEM_ELEMENT)) {
			String type = itemElement.attributeValue(TYPE_ATTR);

			Registry<TriggerItemFactory> registry = PlayPadPlugin.getRegistries().getTriggerItems();
			try {
				TriggerItemFactory connect = registry.getFactory(type);
				TriggerItem item = connect.newInstance(this);
				item.load(itemElement);
				items.add(item);
			} catch (NoSuchComponentException e) {
				Logger.error(e);
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

	public void handle(Pad pad, Duration currentDuration, Project project, IMainViewController mainViewController, Profile currentProfile) {
		for (TriggerItem item : items) {
			switch (triggerPoint) {
				case START:
					handleStartPoint(pad, currentDuration, project, mainViewController, currentProfile, item);
					break;
				case STOP:
					item.performAction(pad, project, mainViewController, currentProfile);
					break;
				case EOF:
					if (item.getDurationFromPoint() == Duration.ZERO) {
						if (pad.isEof()) {
							item.performAction(pad, project, mainViewController, currentProfile);
						}
					} else {
						handleEndPoint(pad, currentDuration, project, mainViewController, currentProfile, item);
					}
					break;
				case PLAYLIST_START:
				case PLAYLIST_ITEM_START:
					handleStartPoint(pad, currentDuration, project, mainViewController, currentProfile, item);
					break;
				case PLAYLIST_ITEM_END:
				case PLAYLIST_END:
					item.performAction(pad, project, mainViewController, currentProfile);
					break;
			}
		}
	}

	private void handleStartPoint(Pad pad, Duration duration, Project project, IMainViewController mainViewController, Profile currentProfile, TriggerItem item) {
		if (pad.getStatus() == PadStatus.PLAY) {
			// Mitten drin, wenn die Zeit die gespielt wurde größer ist als die gesetzte und noch der Trigger noch nicht ausgeführt
			// wurde (null)
			if ((item.getPerformedAt() == null && item.getDurationFromPoint().lessThan(duration))
					// Wenn der Trigger am Anfang ist
					|| (duration.equals(Duration.ZERO) && item.getDurationFromPoint().equals(Duration.ZERO))) {
				item.performAction(pad, project, mainViewController, currentProfile);
				item.setPerformedAt(duration);
			} else if (item.getDurationFromPoint().greaterThan(duration)) {
				item.setPerformedAt(null);
			}
		}
	}

	private void handleEndPoint(Pad pad, Duration duration, Project project, IMainViewController mainViewController, Profile currentProfile, TriggerItem item) {
		// Wenn Trigger noch nicht gespielt wurde (null) und Zeit größer ist als gesetzte Zeit (oder 0)
		if (item.getPerformedAt() == null && (item.getDurationFromPoint().greaterThan(duration) || duration.equals(Duration.ZERO))) {
			item.performAction(pad, project, mainViewController, currentProfile);
			item.setPerformedAt(duration);
		} else if (item.getDurationFromPoint().lessThan(duration)) {
			item.setPerformedAt(null);
		}
	}
}
