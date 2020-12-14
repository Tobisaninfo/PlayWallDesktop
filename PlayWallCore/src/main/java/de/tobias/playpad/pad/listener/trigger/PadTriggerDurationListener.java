package de.tobias.playpad.pad.listener.trigger;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerPoint;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.Duration;

public class PadTriggerDurationListener implements ChangeListener<Duration> {

	private final Pad pad;

	public PadTriggerDurationListener(Pad pad) {
		this.pad = pad;
	}

	@Override
	public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration currentTime) {
		PadContent content = pad.getContent();
		if (content instanceof Durationable) {
			Duration totalDuration = ((Durationable) content).getDuration();
			if (totalDuration != null) {
				IMainViewController mainViewController = PlayPadPlugin.getInstance().getMainViewController();
				Profile currentProfile = Profile.currentProfile();
				PadSettings padSettings = pad.getPadSettings();

				// Execute Start Triggers
				final Trigger startTrigger = padSettings.getTrigger(TriggerPoint.START);
				startTrigger.handle(pad, currentTime, pad.getProject(), mainViewController, currentProfile);

				// Execute End Trigger
				final Duration leftTime = totalDuration.subtract(currentTime);
				final Trigger endTrigger = padSettings.getTrigger(TriggerPoint.EOF);
				endTrigger.handle(pad, leftTime, pad.getProject(), mainViewController, currentProfile);
			}
		}
	}
}
