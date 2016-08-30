package de.tobias.playpad.viewcontroller;

import java.util.ResourceBundle;

import de.tobias.playpad.audio.AudioHandlerConnect;
import de.tobias.utils.ui.ContentViewController;

/**
 * Einstellungen ViewController für einen sAudio Handler.
 * 
 * @author tobias
 * 
 * @see AudioHandlerConnect
 */
public abstract class AudioHandlerViewController extends ContentViewController {

	/**
	 * Neuer ViewController.
	 * 
	 * @param name
	 *            Name der FXML
	 * @param path
	 *            Path zur FXML (ohne Dateiname)
	 * @param localization
	 *            Localization
	 */
	public AudioHandlerViewController(String name, String path, ResourceBundle localization) {
		super(name, path, localization);
	}

	/**
	 * Prüft ob die Einstellungen geändert wurden.
	 * 
	 * @return <code>true</code> Audio Einstellungen für Pads werden automatisch neu geladen
	 */
	public abstract boolean isChanged();
}
