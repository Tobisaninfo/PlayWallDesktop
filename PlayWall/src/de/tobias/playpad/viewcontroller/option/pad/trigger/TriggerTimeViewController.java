package de.tobias.playpad.viewcontroller.option.pad.trigger;

import java.util.Optional;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.tigger.TriggerItem;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.util.TimeUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class TriggerTimeViewController extends ContentViewController {

	@FXML private TextField timeTextField;

	private TriggerItem item;

	public TriggerTimeViewController(TriggerItem item) {
		super("triggerTime", "de/tobias/playpad/assets/view/option/pad/trigger/", PlayPadMain.getUiResourceBundle());
		this.item = item;

		timeTextField.setText(String.valueOf(item.getDurationFromPoint().toSeconds()));
	}

	@Override
	public void init() {
		timeTextField.textProperty().addListener((a, b, c) ->
		{
			Optional<Duration> duration = TimeUtils.parse(c);
			if (duration.isPresent()) {
				item.setDurationFromPoint(duration.get());
			}
			timeTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, !duration.isPresent());
		});
	}
}
