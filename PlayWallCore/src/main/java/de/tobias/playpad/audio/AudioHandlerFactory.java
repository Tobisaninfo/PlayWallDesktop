package de.tobias.playpad.audio;

import de.tobias.playpad.RegistryCollection;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.registry.Component;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;

/**
 * Factory to create an instance of an audio handler implementation. The factories are collected in {@link RegistryCollection#getAudioHandlers()}
 * If an AudioHandler have some cleanups on shutdown to do, it must implement {@link AutoCloseable}
 *
 * @author tobias
 * @since 5.0.0
 */
public abstract class AudioHandlerFactory extends Component {

	public AudioHandlerFactory(String type) {
		super(type);
	}

	/**
	 * Erstellt für eine Kachel ein neunes AudioInterface
	 *
	 * @param content Content des Pads
	 * @return AudioHandler
	 */
	public abstract AudioHandler createAudioHandler(PadContent content);

	/**
	 * Check if an audio feature is available in the implementation
	 *
	 * @param audioCapability Feature
	 * @return <code>true</code> available
	 */
	public abstract boolean isFeatureAvailable(AudioCapability audioCapability);

	/**
	 * Gibt wenn vorhanden einen ViewController für die entsprechenden
	 * Einstellungen zurück.
	 *
	 * @param audioCapablility Audio Feature
	 * @return ViewController
	 */
	public abstract AudioHandlerViewController getAudioFeatureSettings(AudioCapability audioCapablility);
}
