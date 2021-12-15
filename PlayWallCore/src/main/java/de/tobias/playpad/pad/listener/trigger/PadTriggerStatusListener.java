package de.tobias.playpad.pad.listener.trigger;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerPoint;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

public class PadTriggerStatusListener implements ChangeListener<PadStatus> {

	private final Pad pad;

	public PadTriggerStatusListener(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends PadStatus> observable, PadStatus oldState, PadStatus newValue) {
		if (!pad.isIgnoreTrigger()) {
			PadSettings padSettings = pad.getPadSettings();

			// Execute Trigger
			if (newValue == PadStatus.PLAY) {
				executeTrigger(padSettings.getTriggers().get(TriggerPoint.START));
			} else if (newValue == PadStatus.STOP && !pad.isEof()) {
				executeTrigger(padSettings.getTriggers().get(TriggerPoint.STOP));
			} else if (oldState == PadStatus.STOP && newValue == PadStatus.READY && pad.isEof()) {
				executeTrigger(padSettings.getTriggers().get(TriggerPoint.EOF));
			}
		} else {
			pad.setIgnoreTrigger(false);
		}
	}

	private void executeTrigger(Trigger trigger) {
		IMainViewController mainViewController = PlayPadPlugin.getInstance().getMainViewController();
		Profile currentProfile = Profile.currentProfile();

		trigger.handle(pad, Duration.ZERO, pad.getProject(), mainViewController, currentProfile);
	}
}
