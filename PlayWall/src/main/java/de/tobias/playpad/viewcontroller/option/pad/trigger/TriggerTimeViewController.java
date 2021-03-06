package de.tobias.playpad.viewcontroller.option.pad.trigger;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.thecodelabs.utils.util.TimeUtils;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.playpad.view.PseudoClasses;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.util.Optional;

public class TriggerTimeViewController extends NVC {

	@FXML
	private TextField timeTextField;

	private TriggerItem item;

	TriggerTimeViewController(TriggerItem item) {
		load("view/option/pad/trigger", "TriggerTime", Localization.getBundle());
		this.item = item;

		timeTextField.setText(String.valueOf(item.getDurationFromPoint().toSeconds()));
	}

	@Override
	public void init() {
		timeTextField.textProperty().addListener((a, b, c) ->
		{
			Optional<Duration> duration = TimeUtils.parseDuration(c);
			duration.ifPresent(item::setDurationFromPoint);
			timeTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, !duration.isPresent());
		});
	}
}
