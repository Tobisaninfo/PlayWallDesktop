package de.tobias.playpad.pad.listener.trigger;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.content.PlaylistListener;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.tigger.Trigger;
import de.tobias.playpad.tigger.TriggerPoint;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.util.Duration;

public class PadTriggerPlaylistListener implements PlaylistListener {
	@Override
	public void onNextItem(Pad pad, int next, int total) {
		if (!pad.isIgnoreTrigger()) {
			PadSettings padSettings = pad.getPadSettings();
			executeTrigger(pad, padSettings.getTriggers().get(TriggerPoint.PLAYLIST_NEXT));
		} else {
			pad.setIgnoreTrigger(false);
		}
	}

	private void executeTrigger(Pad pad, Trigger trigger) {
		IMainViewController mainViewController = PlayPadPlugin.getInstance().getMainViewController();
		Profile currentProfile = Profile.currentProfile();

		trigger.handle(pad, Duration.ZERO, pad.getProject(), mainViewController, currentProfile);
	}
}
