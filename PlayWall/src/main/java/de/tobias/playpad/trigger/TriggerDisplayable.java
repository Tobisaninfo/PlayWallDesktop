package de.tobias.playpad.trigger;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.Strings;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TriggerDisplayable implements Displayable {

	private Trigger trigger;

	public TriggerDisplayable(Trigger trigger) {
		this.trigger = trigger;
		updateString();
	}

	public Trigger getTrigger() {
		return trigger;
	}

	private StringProperty displayable = new SimpleStringProperty();

	@Override
	public StringProperty displayProperty() {
		return displayable;
	}

	public void addItem(TriggerItem triggerItem) {
		trigger.addItem(triggerItem);
		updateString();
	}

	public void removeItem(TriggerItem item) {
		trigger.removeItem(item);
		updateString();
	}

	private void updateString() {
		displayable.set(toString());
	}

	@Override
	public String toString() {
		String triggerPointName = Localization.getString(Strings.TriggerPoint_BaseName + trigger.getTriggerPoint().name());
		return Localization.getString(Strings.TriggerPoint_toString, triggerPointName, trigger.getItems().size());
	}
}