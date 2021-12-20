package de.tobias.playpad.trigger;

import de.thecodelabs.utils.list.UniqList;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.fade.Fadeable;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.volume.VolumeManager;
import javafx.util.Duration;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VolumeTriggerItem extends TriggerItem {

	private double volume = 1.0;
	private Duration duration = new Duration(2000);
	private List<UUID> uuids;

	private String type;

	VolumeTriggerItem(String type) {
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
		uuids.stream().map(project::getPad)
				.filter(i -> i.getContent() instanceof Fadeable)
				.forEach(destination -> {
					Fadeable fadeable = (Fadeable) destination.getContent();

					final double start = VolumeManager.getInstance().computeVolume(destination);
					fadeable.fade(start, volume, duration, null);
				});
	}

	@Override
	public TriggerItem copy() {
		VolumeTriggerItem clone = new VolumeTriggerItem(getType());

		clone.uuids = new ArrayList<>();
		clone.uuids.addAll(uuids);
		clone.volume = volume;
		clone.duration = new Duration(duration.toMillis());

		return clone;
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

		for (Element cartElement : element.elements(CART_ELEMENT)) {
			uuids.add(UUID.fromString(cartElement.getStringValue()));
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
