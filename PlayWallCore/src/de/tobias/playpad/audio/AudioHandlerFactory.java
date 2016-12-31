package de.tobias.playpad.audio;

import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.registry.Component;
import de.tobias.playpad.viewcontroller.AudioHandlerViewController;
import de.tobias.utils.ui.icon.FontIconType;

/**
 * Audio Handler Interface zur Verwaltung einer AudioHandler Implementierung.
 * Für Aktionen beim schließen des Programmes, muss der AudioHandler
 * AutoClosable implementieren.
 * 
 * @author tobias
 *
 * @since 5.0.0
 */
public abstract class AudioHandlerFactory extends Component {

	public AudioHandlerFactory(String type) {
		super(type);
	}

	/**
	 * Erstellt für eine Kachel ein neunes AudioInterface
	 * 
	 * @param content
	 *            Content des Pads
	 * @return AudioHandler
	 */
	public abstract AudioHandler createAudioHandler(PadContent content);

	/**
	 * Gibt den Settings View Controller für die Audio Schnittstelle zurück.s
	 * 
	 * @return neuer ViewContoller
	 */
	public abstract AudioHandlerViewController getAudioHandlerSettingsViewController();

	/**
	 * Prüft ob ein Feature verfügbar ist.
	 * 
	 * @param audioCapability
	 *            Feature
	 * @return <code>true</code> Verfügbar
	 */
	public abstract boolean isFeatureAvaiable(AudioCapability audioCapability);

	/**
	 * Gibt wenn vorhanden einen ViewController für die entsprechenden
	 * Einstellungen zurück.
	 * 
	 * @param audioCapablility
	 *            Audio Feature
	 * @return ViewController
	 */
	public abstract AudioHandlerViewController getAudioFeatureSettings(AudioCapability audioCapablility);
}
