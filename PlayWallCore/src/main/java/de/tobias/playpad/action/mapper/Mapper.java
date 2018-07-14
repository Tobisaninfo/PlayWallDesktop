package de.tobias.playpad.action.mapper;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.feedback.Feedback;
import de.tobias.playpad.action.feedback.FeedbackType;
import org.dom4j.Element;

/**
 * Abstrakte Klasse für das Handling von Mappern. Die Aktionen und Handler werden von der Entsprechenden Aktion verwaltet, die dazu gehört.
 * Jeder Mapper muss zu einer Aktion gehören. Zu jedem Mapper gehört auch ein Feedback. Dieses wird mittels Interfaces in der konktreten
 * Implementation definiert. Diese abstrakte Klasse verwaltet allerdings nur den Type des Feedbacks. Die eigentloche Implementierung des
 * Feedbacks ist Aufgabe des konkreten Mappers.
 *
 * @author tobias
 * @see Action Aktion, zu der ein Mapper gehört.
 * @see Feedback Feedback für ein Mapper.
 * @since 5.0.0
 */
public abstract class Mapper implements Displayable, Cloneable {

	/**
	 * Feedback für diesen Mapper. (Beispiel Feedback für eine Taste an einem MIDI Gerät.
	 */
	protected FeedbackType feedbackType;

	/**
	 * Setzt den FeedbackType des Mappers und initalisiert ihn.
	 *
	 * @param feedbackType neuer FeedbackType
	 * @see Mapper#initFeedback() wird automatisch aufgerufen.
	 */
	@Deprecated // Referenz auf Action, da Action den FeedbackType schon hat
	public void setFeedbackType(FeedbackType feedbackType) {
		this.feedbackType = feedbackType;
		initFeedback();
	}

	/**
	 * Gibt den FeedbackType für den entsprechenden Mapper zurück.
	 *
	 * @return FeedbackType für den Mapper.
	 */
	@Deprecated
	public FeedbackType getFeedbackType() {
		return feedbackType;
	}

	/**
	 * Muss die entsprechende Implementierung des Feedbacks, sofern überhaupt benütigt initialisieren. Standart Implementierung ist leer, da
	 * optionale Methode.
	 */
	public void initFeedback() {

	}

	/**
	 * ID des Mappers.
	 *
	 * @return IDs
	 */
	public abstract String getType();

	/**
	 * Deserialisierung der Daten
	 *
	 * @param element XML Element
	 * @param action  Zugehörige Action
	 */
	public abstract void load(Element element, Action action);

	/**
	 * Speichert die Einstellungen eines Mappers.
	 *
	 * @param element Oberstes Objekt der XML Daten
	 */
	public abstract void save(Element element);

	/**
	 * Dupliziert ein Mapper Objekt.
	 *
	 * @return Duplikat.
	 * @throws CloneNotSupportedException Clone Fehlgeschlagen
	 */
	public abstract Mapper cloneMapper() throws CloneNotSupportedException;

}
