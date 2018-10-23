package de.tobias.playpad.viewcontroller;

import de.thecodelabs.utils.ui.NVC;
import de.tobias.playpad.audio.AudioHandlerFactory;

import java.util.ResourceBundle;

/**
 * Einstellungen ViewController für einen sAudio Handler.
 *
 * @author tobias
 * @see AudioHandlerFactory
 */
public abstract class AudioHandlerViewController extends NVC {

	/**
	 * Neuer ViewController.
	 *
	 * @param name         Name der FXML
	 * @param path         Path zur FXML (ohne Dateiname)
	 * @param localization Localization
	 */
	public AudioHandlerViewController(String name, String path, ResourceBundle localization) {
		load(path, name, localization);
	}

	/**
	 * Prüft ob die Einstellungen geändert wurden.
	 *
	 * @return <code>true</code> Audio Einstellungen für Pads werden automatisch neu geladen
	 */
	public abstract boolean isChanged();

	/**
	 * This method is called, then the window will be closed.
	 */
	public void onClose() {

	}
}
