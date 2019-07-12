package de.tobias.playpad.plugin.media.video;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.plugin.media.main.impl.MediaPluginImpl;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

public class VideoPadSettingsTabViewController extends PadSettingsTabViewController {

	@FXML
	private CheckBox lastFrameCheckBox;

	VideoPadSettingsTabViewController() {
		load("view/", "SettingsPadPane", MediaPluginImpl.getInstance().getBundle());
	}

	@Override
	public String getName() {
		return MediaPluginImpl.getInstance().getBundle().getString("settings.video.tab");
	}

	@Override
	public void loadSettings(Pad pad) {
		if (pad.getPadSettings().getCustomSettings().containsKey(VideoContent.VIDEO_LAST_FRAME))
			lastFrameCheckBox.setSelected((boolean) pad.getPadSettings().getCustomSettings().get(VideoContent.VIDEO_LAST_FRAME));
	}

	@Override
	public void saveSettings(Pad pad) {
		pad.getPadSettings().getCustomSettings().put(VideoContent.VIDEO_LAST_FRAME, lastFrameCheckBox.isSelected());
	}

}