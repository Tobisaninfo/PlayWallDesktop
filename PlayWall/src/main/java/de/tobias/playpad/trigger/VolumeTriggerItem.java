package de.tobias.playpad.trigger;

import de.thecodelabs.utils.list.UniqList;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.fade.Fadeable;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.util.Duration;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class VolumeTriggerItem extends TriggerItem {

	private double volume = 1.0;
	private Duration duration = new Duration(2000);
	private List<UUID> uuids;

	private static HashMap<Integer, Double> volumeCache = new HashMap<>();

	private String type;

	public VolumeTriggerItem(String type) {
		super();
		this.type = type;
		this.uuids = new UniqList<>();
	}

	@Override
	public String getType() {
		return type;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public Duration getDuration() {
		return duration;
	}

	public void setDuration(Duration duration) {
		this.duration = duration;
	}

	public List<UUID> getCarts() {
		return uuids;
	}

	@Override
	public void performAction(Pad pad, Project project, IMainViewController controller, Profile profile) {
		for (Pad destination : uuids.stream().map(project::getPad).collect(Collectors.toList())) {
			if (destination.getContent() instanceof Fadeable) {

				final int id = destination.getPadIndex().getId();
				if (!volumeCache.containsKey(id)) {
					volumeCache.put(id, destination.getPadSettings().getVolume());
				}
				double start = volumeCache.get(id);


				Fadeable fadeable = (Fadeable) destination.getContent();
				fadeable.fade(start, volume, duration, () -> {
					volumeCache.put(id, volume);
				});
			}
		}
	}

	private static final String CART_ELEMENT = "Cart";
	private static final String VOLUME_ATTR = "Volume";
	private static final String DURATION_ATTR = "Duration";

	@Override
	public void load(Element element) {
		super.load(element);
		if (element.attributeValue(VOLUME_ATTR) != null)
			volume = Double.parseDouble(element.attributeValue(VOLUME_ATTR));
		if (element.attributeValue(DURATION_ATTR) != null)
			duration = Duration.millis(Double.parseDouble(element.attributeValue(DURATION_ATTR)));

		for (Object cartObj : element.elements(CART_ELEMENT)) {
			if (cartObj instanceof Element) {
				Element cartElement = (Element) cartObj;
				uuids.add(UUID.fromString(cartElement.getStringValue()));
			}
		}
	}

	@Override
	public void save(Element element) {
		super.save(element);

		element.addAttribute(VOLUME_ATTR, String.valueOf(volume));
		element.addAttribute(DURATION_ATTR, String.valueOf(duration.toMillis()));

		for (UUID cart : uuids) {
			Element cartElement = element.addElement(CART_ELEMENT);
			cartElement.addText(String.valueOf(cart));
		}
	}
}
