package de.tobias.playpad.action.feedback;

import de.tobias.playpad.action.mapper.Mapper;
import org.dom4j.Element;

/**
 * Das ist die Abstrakte Klasse für ein Feedback. Jedes Mapper Gerät kann eine eigene Klasse dafür entwickeln. Allerdings müssen gibt es ur
 * eine beschränkte Anzahl an Typen von Feedbacks.
 *
 * @author tobias
 * @see FeedbackType Type des Feedbacks. Damit wird es im Mapper initalisiert.
 * @see Mapper#initFeedback() Damit wird das Feedback initalisiert im Mapper.
 * @since 5.0.0
 */
public abstract class Feedback {

	/**
	 * Gibt den Wert für das Gerät zurück, für eine bestimmte Aktion.
	 *
	 * @param message Art der Feedback Meldung
	 * @return Wert für den Mapper
	 */
	public abstract int getValueForFeedbackMessage(FeedbackMessage message);

	/**
	 * Setzt den Wert für eine Feedback Meldung.
	 *
	 * @param feedbackMessage Art der Feedback Meldung
	 * @param value           Wert für den Mapper
	 */
	public abstract void setFeedback(FeedbackMessage feedbackMessage, int value);

	/**
	 * Lädt alle Informationen aus einem XML Objekt.
	 *
	 * @param feedbackObject XML Object.
	 */
	public abstract void load(Element feedbackObject);

	/**
	 * Speichert die Informationen des Feedbacks in ein XML Objekt.
	 *
	 * @param feedbackObject Oberstes Objekt der XML Daten
	 */
	public abstract void save(Element feedbackObject);

	/**
	 * Dupliziert ein FeedbackObjeck.
	 *
	 * @return Duplikat.
	 * @throws CloneNotSupportedException Clone Fehlerhaft
	 */
	public abstract Feedback cloneFeedback() throws CloneNotSupportedException;

}
