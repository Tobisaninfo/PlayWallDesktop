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
		handleTrigger(pad, TriggerPoint.PLAYLIST_START);
	}

	@Override
	public void onPlaylistItemStart(Pad pad) {
		handleTrigger(pad, TriggerPoint.PLAYLIST_ITEM_START);
	}

	@Override
	public void onPlaylistItemEnd(Pad pad) {
		handleTrigger(pad, TriggerPoint.PLAYLIST_ITEM_END);
	}

	@Override
	public void onPlaylistEnd(Pad pad) {
		handleTrigger(pad, TriggerPoint.PLAYLIST_END);
	}

	private void handleTrigger(Pad pad, TriggerPoint point) {
		if (!pad.isIgnoreTrigger()) {
			final PadSettings padSettings = pad.getPadSettings();
			final Trigger trigger = padSettings.getTriggers().get(point);

			final IMainViewController mainViewController = PlayPadPlugin.getInstance().getMainViewController();
			final Profile currentProfile = Profile.currentProfile();

			trigger.handle(pad, Duration.ZERO, pad.getProject(), mainViewController, currentProfile);
		} else {
			pad.setIgnoreTrigger(false);
		}
	}
}
