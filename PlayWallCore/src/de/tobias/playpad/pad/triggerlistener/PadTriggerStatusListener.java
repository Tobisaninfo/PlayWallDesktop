package de.tobias.playpad.pad.triggerlistener;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerPoint;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

public class PadTriggerStatusListener implements ChangeListener<PadStatus> {

	private Pad pad;

	public PadTriggerStatusListener(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends PadStatus> observable, PadStatus oldValue, PadStatus newValue) {
		if (!pad.isIgnoreTrigger()) {
			if (newValue == PadStatus.PLAY) { // TRIGGER FÜR START
				executeTrigger(pad.getTriggers().get(TriggerPoint.START));
			} else if (newValue == PadStatus.STOP) { // TRIGGER FÜR STOP
				executeTrigger(pad.getTriggers().get(TriggerPoint.EOF_STOP));
			} else if (oldValue == PadStatus.PLAY && newValue == PadStatus.READY && pad.isEof()) { // TRIGGER FÜR EOF
				executeTrigger(pad.getTriggers().get(TriggerPoint.EOF_STOP));
			}
		} else {
			pad.setIgnoreTrigger(false);
		}
	}

	private void executeTrigger(Trigger trigger) {
		IMainViewController mainViewController = PlayPadPlugin.getImplementation().getMainViewController();
		Profile currentProfile = Profile.currentProfile();

		trigger.handle(pad, Duration.ZERO, pad.getProject(), mainViewController, currentProfile);
	}
}
