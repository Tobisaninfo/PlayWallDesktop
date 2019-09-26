package de.tobias.playpad.audio.mac;

import de.tobias.playpad.audio.AudioCapability;
import de.tobias.playpad.audio.AudioHandler;
import de.tobias.playpad.audio.AudioHandlerFactory;
import de.tobias.playpad.audio.mac.delegate.AVAudioPlayerBridgeDelegate;
import de.tobias.playpad.audio.mac.settings.NativeAudioMacSettingsViewController;
import de.tobias.playpad.audio.windows.NativeAudioWinHandler;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NativeAudioMacHandlerFactory extends AudioHandlerFactory {

	private List<NativeAudioMacHandler> handlers = new ArrayList<>();
	private AVAudioPlayerBridgeDelegate bridgeDelegate = new AVAudioPlayerBridgeDelegate(this);

	public Optional<NativeAudioMacHandler> getHandlerByBridge(AVAudioPlayerBridge bridge) {
		return handlers.stream().filter(handler -> handler.getBridge().equals(bridge)).findFirst();
	}

	public NativeAudioMacHandlerFactory(String type) {
		super(type);
	}

	@Override
	public AudioHandler createAudioHandler(PadContent content) {
		NativeAudioMacHandler nativeAudioMacHandler = new NativeAudioMacHandler(content);
		nativeAudioMacHandler.getBridge().setDelegate(bridgeDelegate);
		handlers.add(nativeAudioMacHandler);
		return nativeAudioMacHandler;
	}

	@Override
	public boolean isFeatureAvailable(AudioCapability audioCapability) {
		for (Class<?> clazz : NativeAudioWinHandler.class.getInterfaces()) {
			if (clazz.equals(audioCapability.getAudioFeature()))
				return true;
		}
		return false;
	}

	@Override
	public AudioHandlerViewController getAudioFeatureSettings(AudioCapability audioCapability) {
		if (audioCapability == AudioCapability.SOUNDCARD) {
			return new NativeAudioMacSettingsViewController();
		}
		return null;
	}
}
