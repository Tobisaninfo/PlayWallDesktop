package de.tobias.playpad.audio.mac.delegate;

import de.tobias.playpad.audio.Peakable;
import de.tobias.playpad.audio.mac.AVAudioPlayerBridge;
import de.tobias.playpad.audio.mac.NativeAudioMacHandler;
import de.tobias.playpad.audio.mac.NativeAudioMacHandlerFactory;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContent;
import javafx.util.Duration;

import java.util.Optional;

public class AVAudioPlayerBridgeDelegate implements AVAudioPlayerBridge.NativeAudioDelegate {

	private NativeAudioMacHandlerFactory factory;

	public AVAudioPlayerBridgeDelegate(NativeAudioMacHandlerFactory factory) {
		this.factory = factory;
	}

	@Override
	public void onFinish(AVAudioPlayerBridge bridge) {
		Optional<NativeAudioMacHandler> nativeAudioMacHandler = factory.getHandlerByBridge(bridge);
		nativeAudioMacHandler.ifPresent(handler -> {
			PadContent content = handler.getContent();
			if (content != null) {
				content.getPad().setEof(true);
				content.getPad().setStatus(PadStatus.STOP);
			}
		});
	}

	@Override
	public void onPositionChanged(AVAudioPlayerBridge bridge, double position) {
		Optional<NativeAudioMacHandler> nativeAudioMacHandler = factory.getHandlerByBridge(bridge);
		nativeAudioMacHandler.ifPresent(audioMacHandler -> {
			audioMacHandler.positionProperty().set(Duration.seconds(position));
		});
	}

	@Override
	public void onPeakMeter(AVAudioPlayerBridge bridge, float left, float right) {
		Optional<NativeAudioMacHandler> nativeAudioMacHandler = factory.getHandlerByBridge(bridge);
		nativeAudioMacHandler.ifPresent(handler -> {
			handler.audioLevelProperty(Peakable.Channel.LEFT).set(left);
			handler.audioLevelProperty(Peakable.Channel.RIGHT).set(right);
		});
	}
}
