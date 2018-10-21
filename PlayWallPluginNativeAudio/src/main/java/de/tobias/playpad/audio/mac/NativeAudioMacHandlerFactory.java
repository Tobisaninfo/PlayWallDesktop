package de.tobias.playpad.audio.mac;

import de.tobias.playpad.NativeAudio;
import de.tobias.playpad.NativeAudio.NativeAudioDelegate;
import de.tobias.playpad.audio.AudioCapability;
import de.tobias.playpad.audio.AudioHandler;
import de.tobias.playpad.audio.AudioHandlerFactory;
import de.tobias.playpad.audio.Peakable.Channel;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;
import javafx.util.Duration;

import java.util.HashMap;

public class NativeAudioMacHandlerFactory extends AudioHandlerFactory implements NativeAudioDelegate {

	private static final HashMap<Integer, NativeAudioMacHandler> handlers = new HashMap<>();

	public NativeAudioMacHandlerFactory(String type) {
		super(type);
		NativeAudio.initialize();
		NativeAudio.setDelegate(this);
	}

	@Override
	public AudioHandler createAudioHandler(PadContent content) {
		NativeAudioMacHandler nativeAudioMacHandler = new NativeAudioMacHandler(content);
		handlers.put(nativeAudioMacHandler.getId(), nativeAudioMacHandler);
		return nativeAudioMacHandler;
	}

	@Override
	public void onFinish(int id) {
		NativeAudioMacHandler nativeAudioMacHandler = handlers.get(id);
		if (nativeAudioMacHandler != null) {
			PadContent content = nativeAudioMacHandler.getContent();
			if (content != null) {
				content.getPad().setStatus(PadStatus.STOP);
			}
		}
	}

	@Override
	public void onPositionChanged(int id, double position) {
		NativeAudioMacHandler nativeAudioMacHandler = handlers.get(id);
		if (nativeAudioMacHandler != null) {
			nativeAudioMacHandler.positionProperty.set(Duration.seconds(position));
		}
	}

	@Override
	public void onPeakMeter(int id, float left, float right) {
		NativeAudioMacHandler nativeAudioMacHandler = handlers.get(id);
		if (nativeAudioMacHandler != null) {
			nativeAudioMacHandler.audioLevelProperty(Channel.LEFT).set(left);
			nativeAudioMacHandler.audioLevelProperty(Channel.RIGHT).set(right);
		}
	}

	@Override
	public boolean isFeatureAvailable(AudioCapability audioCapability) {
		return false;
	}

	@Override
	public AudioHandlerViewController getAudioFeatureSettings(AudioCapability audioCapability) {
		return null;
	}
}
