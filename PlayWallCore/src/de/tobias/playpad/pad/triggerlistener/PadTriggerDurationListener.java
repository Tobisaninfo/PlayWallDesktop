package de.tobias.playpad.pad.triggerlistener;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.conntent.Durationable;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerPoint;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

public class PadTriggerDurationListener implements ChangeListener<Duration> {

	private Pad pad;

	public PadTriggerDurationListener(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
		PadContent content = pad.getContent();
		if (content instanceof Durationable) {
			Duration totalDuration = ((Durationable) content).getDuration();
			if (totalDuration != null) {
				Duration leftTime = totalDuration.subtract(newValue);

				IMainViewController mainViewController = PlayPadPlugin.getImplementation().getMainViewController();
				Profile currentProfile = Profile.currentProfile();

				Trigger startTrigger = pad.getTrigger(TriggerPoint.START);
				startTrigger.handle(pad, newValue, pad.getProject(), mainViewController, currentProfile);

				Trigger endTrigger = pad.getTrigger(TriggerPoint.EOF_STOP);
				endTrigger.handle(pad, leftTime, pad.getProject(), mainViewController, currentProfile);
			}
		}
	}
}
