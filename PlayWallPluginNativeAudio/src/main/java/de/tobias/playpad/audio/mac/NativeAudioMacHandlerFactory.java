package de.tobias.playpad.audio.mac;

import de.tobias.playpad.audio.AudioCapability;
import de.tobias.playpad.audio.AudioHandler;
import de.tobias.playpad.audio.AudioHandlerFactory;
import de.tobias.playpad.audio.Peakable.Channel;
import de.tobias.playpad.audio.mac.AVAudioPlayerBridge.NativeAudioDelegate;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NativeAudioMacHandlerFactory extends AudioHandlerFactory implements NativeAudioDelegate {

	private List<NativeAudioMacHandler> handlers = new ArrayList<>();

	private Optional<NativeAudioMacHandler> getHandlerByBridge(AVAudioPlayerBridge bridge) {
		return handlers.stream().filter(handler -> handler.getBridge().equals(bridge)).findFirst();
	}

	public NativeAudioMacHandlerFactory(String type) {
		super(type);
		AVAudioPlayerBridge.setDelegate(this);
	}

	@Override
	public AudioHandler createAudioHandler(PadContent content) {
		NativeAudioMacHandler nativeAudioMacHandler = new NativeAudioMacHandler(content);
		handlers.add(nativeAudioMacHandler);
		return nativeAudioMacHandler;
	}

	@Override
	public void onFinish(AVAudioPlayerBridge bridge) {
		Optional<NativeAudioMacHandler> nativeAudioMacHandler = getHandlerByBridge(bridge);
		nativeAudioMacHandler.ifPresent(handler -> {
			PadContent content = handler.getContent();
			if (content != null) {
				content.getPad().setStatus(PadStatus.STOP);
			}
		});
	}

	@Override
	public void onPositionChanged(AVAudioPlayerBridge bridge, double position) {
		Optional<NativeAudioMacHandler> nativeAudioMacHandler = getHandlerByBridge(bridge);
		nativeAudioMacHandler.ifPresent(audioMacHandler -> {
			audioMacHandler.positionProperty.set(Duration.seconds(position));
		});
	}

	@Override
	public void onPeakMeter(AVAudioPlayerBridge bridge, float left, float right) {
		Optional<NativeAudioMacHandler> nativeAudioMacHandler = getHandlerByBridge(bridge);
		nativeAudioMacHandler.ifPresent(handler -> {
			handler.audioLevelProperty(Channel.LEFT).set(left);
			handler.audioLevelProperty(Channel.RIGHT).set(right);
		});
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
