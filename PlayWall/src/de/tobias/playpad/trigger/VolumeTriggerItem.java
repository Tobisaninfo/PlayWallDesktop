package de.tobias.playpad.trigger;

import org.dom4j.Element;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.animation.Transition;
import javafx.util.Duration;

public class VolumeTriggerItem extends TriggerItem {

	private double volume = 1.0;
	private Duration duration = new Duration(2000);

	private transient static Transition transition;
	private transient static VolumeTriggerItem currentRunningTrigger;
	private transient static double currentValue = 1.0;

	@Override
	public String getType() {
		return VolumeTriggerItemConnect.TYPE;
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

	@Override
	public void performAction(Pad pad, Project project, IMainViewController controller, Profile profile) {
		if (transition != null) {
			currentValue = currentRunningTrigger.volume;
			currentRunningTrigger = null;
		}
		transition = new Transition() {

			{
				setCycleDuration(duration);
			}

			@Override
			protected void interpolate(double frac) {
				for (Pad p : project.getPads().values()) {
					if (p.getIndex() != pad.getIndex()) {
						if (p.getCustomVolume() > volume) {
							p.setCustomVolume(currentValue - frac * (currentValue - volume));
						} else {
							p.setCustomVolume(currentValue + frac * (volume - currentValue));
						}
					}
				}
			}
		};
		transition.setOnFinished(e ->
		{
			transition = null;
			currentValue = volume;
			currentRunningTrigger = null;
		});

		currentRunningTrigger = this;
		transition.play();
	}

	private static final String VOLUME_ATTR = "Volume";
	private static final String DURATION_ATTR = "Duration";

	@Override
	public void load(Element element) {
		super.load(element);
		if (element.attributeValue(VOLUME_ATTR) != null)
			volume = Double.valueOf(element.attributeValue(VOLUME_ATTR));
		if (element.attributeValue(DURATION_ATTR) != null)
			duration = Duration.millis(Double.valueOf(element.attributeValue(DURATION_ATTR)));
	}

	@Override
	public void save(Element element) {
		super.save(element);
		element.addAttribute(VOLUME_ATTR, String.valueOf(volume));
		element.addAttribute(DURATION_ATTR, String.valueOf(duration.toMillis()));
	}
}
