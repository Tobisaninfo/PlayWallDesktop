package de.tobias.playpad.audio;

import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.viewcontroller.AudioTypeViewController;

public abstract class AudioHandlerConnect {

	public abstract AudioHandler createAudioHandler(PadContent content);

	public abstract AudioTypeViewController getAudioViewController();

}
