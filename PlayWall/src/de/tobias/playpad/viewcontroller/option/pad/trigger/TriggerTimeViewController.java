package de.tobias.playpad.viewcontroller.option.pad.trigger;

import java.util.Optional;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.util.TimeUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class TriggerTimeViewController extends NVC {

	@FXML private TextField timeTextField;

	private TriggerItem item;

	TriggerTimeViewController(TriggerItem item) {
		load("de/tobias/playpad/assets/view/option/pad/trigger/", "triggerTime", PlayPadMain.getUiResourceBundle());
		this.item = item;

		timeTextField.setText(String.valueOf(item.getDurationFromPoint().toSeconds()));
	}

	@Override
	public void init() {
		timeTextField.textProperty().addListener((a, b, c) ->
		{
			Optional<Duration> duration = TimeUtils.parse(c);
			duration.ifPresent(item::setDurationFromPoint);
			timeTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, !duration.isPresent());
		});
	}
}
