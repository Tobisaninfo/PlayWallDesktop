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
	public void onPlaylistStart(Pad pad) {

	}

	@Override
	public void onPlaylistItemStart(Pad pad) {
		if (!pad.isIgnoreTrigger()) {
			final PadSettings padSettings = pad.getPadSettings();
			final Trigger trigger = padSettings.getTriggers().get(TriggerPoint.PLAYLIST_ITEM_START);
			executeTrigger(pad, trigger);
		} else {
			pad.setIgnoreTrigger(false);
		}
	}

	@Override
	public void onPlaylistItemEnd(Pad pad) {
		if (!pad.isIgnoreTrigger()) {
			final PadSettings padSettings = pad.getPadSettings();
			final Trigger trigger = padSettings.getTriggers().get(TriggerPoint.PLAYLIST_ITEM_END);
			executeTrigger(pad, trigger);
		} else {
			pad.setIgnoreTrigger(false);
		}
	}

	@Override
	public void onPlaylistEnd(Pad pad) {

	}

	private void executeTrigger(Pad pad, Trigger trigger) {
		IMainViewController mainViewController = PlayPadPlugin.getInstance().getMainViewController();
		Profile currentProfile = Profile.currentProfile();

		trigger.handle(pad, Duration.ZERO, pad.getProject(), mainViewController, currentProfile);
	}
}
