package de.tobias.playpad.pad;

import org.dom4j.Element;

import de.tobias.utils.settings.SettingsSerializable;
import de.tobias.utils.settings.Storable;
import javafx.util.Duration;

public class Warning implements SettingsSerializable{

	private static final long serialVersionUID = 1L;
	
	@Storable private Duration time;

	public Warning() {
		time = Duration.seconds(5);
	}

	public Warning(Duration time) {
		this.time = time;
	}

	public Duration getTime() {
		return time;
	}

	public void setTime(Duration time) {
		this.time = time;
	}

	private static final String TIME_ELEMENT = "Time";

	public static Warning load(Element feedbackElement) {
		try {
			if (feedbackElement.element(TIME_ELEMENT) != null) {
				Duration dutation = Duration.valueOf(feedbackElement.element(TIME_ELEMENT).getStringValue().replace(" ", ""));
				Warning warning = new Warning(dutation);
				return warning;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Element save(Element feedbackElement) {
		feedbackElement.addElement(TIME_ELEMENT).addText(time.toString());
		return feedbackElement;
	}
}
