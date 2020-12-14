package de.tobias.playpad.pad.content;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.preview.PadTextPreview;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import de.tobias.playpad.viewcontroller.option.profile.AudioTabViewController;
import javafx.scene.layout.Pane;

public class AudioPadContentFactory extends PadContentFactory {

	private static final String[] FILE_EXTENSION = {"*.mp3", "*.wav"};

	public AudioPadContentFactory(String type) {
		super(type);
	}

	@Override
	public PadContent newInstance(Pad pad) {
		return new AudioContent(getType(), pad);
	}

	@Override
	public String[] getSupportedTypes() {
		return FILE_EXTENSION;
	}

	@Override
	public IPadContentView getPadContentPreview(Pad pad, Pane parentNode) {
		if (pad.getContent() != null) {
			return new PadTextPreview(pad, parentNode);
		} else {
			return null;
		}
	}

	@Override
	public ProfileSettingsTabViewController getSettingsTabViewController(boolean activePlayer) {
		return new AudioTabViewController(activePlayer);
	}
}
