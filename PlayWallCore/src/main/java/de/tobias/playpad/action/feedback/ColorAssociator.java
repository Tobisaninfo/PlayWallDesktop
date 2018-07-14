package de.tobias.playpad.action.feedback;

import javafx.scene.paint.Color;

/**
 * Dieses Interface wird in einem Mapper implementiert. Dabei handelt er die Anfragen für das Mapping von Farben.
 *
 * @author tobias
 * @since 5.0.0
 */
public interface ColorAssociator {

	/**
	 * Gibt die Gerätefarben zurück. Dabei enthalten diese ein Int Value und ein Paint.
	 *
	 * @return Liste an Farben
	 */
	DisplayableFeedbackColor[] getColors();

	/**
	 * Standardfarbe, falls nichts passendes gefunden wurde.
	 *
	 * @return Standardfarbe
	 */
	DisplayableFeedbackColor getDefaultStandardColor();

	/**
	 * Eventfarbe, falls nichts passendes gefunden wurde.
	 *
	 * @return Eventfarbe
	 */
	DisplayableFeedbackColor getDefaultEventColor();

	/**
	 * Setzt die Feedback Farbe für die Instanz des Mappers.
	 *
	 * @param feedbackMessage Art der Feedbacknachricht
	 * @param color           Matched Color
	 */
	void setColor(FeedbackMessage feedbackMessage, DisplayableFeedbackColor color);

	/**
	 * Sucht zu einer {@link Color} die passende FeedbackColor, falls vorhanden.
	 *
	 * @param color Kachel Farbe
	 * @return Feedback Farbe oder null.
	 */
	DisplayableFeedbackColor map(Color color);
}
