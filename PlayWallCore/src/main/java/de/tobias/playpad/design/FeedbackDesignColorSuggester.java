package de.tobias.playpad.design;

import javafx.scene.paint.Color;

/**
 * Methoden für die Verwaltung der Farben, die an einer Kachel eingestellt sind. Das ist wichtig, falls Kachel eine andere Farbverwaltung
 * verwendet (beispiel Lineare Gradient).
 *
 * @author tobias
 * @since 5.0.0
 */
public interface FeedbackDesignColorSuggester {

	/**
	 * Gibt die Standardfarbe (Kacheln ohne Aktion) zurück.
	 *
	 * @return Farbe der Kachel
	 */
	Color getDesignDefaultColor();

	/**
	 * Gibt die Eventfarbe (Kacheln mit Aktion) zurück.
	 *
	 * @return Farbe der Kachel
	 */
	Color getDesignEventColor();

}
