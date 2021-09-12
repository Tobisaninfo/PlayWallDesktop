package de.tobias.playpad.plugin.media.video;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.pad.preview.PadTextPreview;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.plugin.media.main.impl.MediaPluginImpl;
import de.tobias.playpad.plugin.media.main.impl.MediaSettingsTabViewController;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import javafx.scene.layout.Pane;

public class VideoPadContentFactory extends PadContentFactory {

	private static final String[] FILE_EXTENSION = {"*.mp4", "*.mov"};

	public VideoPadContentFactory(String type) {
		super(type);
	}

	@Override
	public PadContent newInstance(Pad pad) {
		return new VideoContent(getType(), pad);
	}

	@Override
	public IPadContentView getPadContentPreview(Pad pad, Pane parentNode) {
		return new PadTextPreview(pad, parentNode);
	}

	@Override
	public ProfileSettingsTabViewController getSettingsTabViewController(boolean activePlayer) {
		MediaPluginImpl instance = MediaPluginImpl.getInstance();
		return new MediaSettingsTabViewController(instance.getCurrentSettings());
	}

	@Override
	public PadSettingsTabViewController getSettingsViewController(Pad pad) {
		return new VideoPadSettingsTabViewController();
	}

	@Override
	public String[] getSupportedTypes() {
		return FILE_EXTENSION;
	}

}
