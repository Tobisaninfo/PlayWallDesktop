package de.tobias.playpad.pad.view;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import de.tobias.playpad.project.page.PadIndex;
import javafx.css.PseudoClass;
import javafx.scene.layout.Pane;

/**
 * Zugriff auf eine PadView. Hier sind alle Methoden um mit der GUI für ein Pad zu agieren.
 *
 * @author tobias
 * @since 5.1.0
 */
public interface IPadView {

	/**
	 * Gibt die aktuelle Vorschau für den Content eines Pads zurück.
	 *
	 * @return ContentView
	 */
	IPadContentView getContentView();

	/**
	 * Setzt die Preview für den PadContent.
	 *
	 * @param pad Pad
	 */
	void setContentView(Pad pad);

	/**
	 * Gibt den zugehörigen ViewController zu einem Pad zurück.
	 *
	 * @return ViewController des Pad
	 */
	IPadViewController getViewController();

	/**
	 * Gibt das oberste GUI Element des Pads zurück, welche im MainView verwendet wird.
	 *
	 * @return root node
	 */
	Pane getRootNode();

	/**
	 * Schaltet den Design Modus für Drag And Drop ein.
	 *
	 * @param enable true eingeschaltet
	 */
	void enableDragAndDropDesignMode(boolean enable);

	/**
	 * Zeigt ein BusyView über dem Padview an.
	 *
	 * @param enable true, wird angezeigt
	 */
	void showBusyView(boolean enable);

	/**
	 * Aktiviert eine Pseudoclass für die View
	 *
	 * @param playClass Pseudo class
	 * @param b         <code>true</code> active
	 */
	void pseudoClassState(PseudoClass playClass, boolean b);

	/**
	 * Setzt den Style für den Root Node der PadView.
	 *
	 * @param string Style
	 */
	void setStyle(String string);

	/**
	 * Hebt eine Kachel hervor (Beispiel mit einer Animation.
	 *
	 * @param milliSeconds Dauer in Millisekunden
	 */
	void highlightView(int milliSeconds);

	/**
	 * Aktiviert des Error Labels, damit es angezeigt wird.
	 *
	 * @param b <code>true</code> Sichtbar
	 */
	void setErrorLabelActive(boolean b);

	/**
	 * Setzt den Fortschritt auf der PlayBar
	 *
	 * @param value [0, 1]
	 */
	void setPlayBarProgress(double value);

	/**
	 * Set the cue in progress value.
	 *
	 * @param value value between 0 and 1
	 */
	void setCueInProgress(double value);

	/**
	 * Setzt die Playbar sichtbar.
	 *
	 * @param visible <code>true</code> Sichtbar, <code>false</code> nicht sichtbar
	 */
	void setPlaybarVisible(boolean visible);

	/**
	 * Fügt die Standart Elemente der PadView hinzu. Die GUI Element sind Abhängig vom Pad, und welchen Content es hat.
	 *
	 * @param pad Pad
	 */
	void addDefaultElements(Pad pad);

	/**
	 * Fügt die StyleClasses der PadView hinzu. Die Methode wird vom Controller aufgerufen.
	 *
	 * @param index Index von der Kachel
	 */
	void applyStyleClasses(PadIndex index);

	/**
	 * Entfernt die StyleClasses vom PadView. Die Methode wird vom Controller aufgerufen.
	 */
	void removeStyleClasses();

	/**
	 * Shows the not found icon overlay
	 *
	 * @param pad  pad
	 * @param show show
	 */
	void showNotFoundIcon(Pad pad, boolean show);
}
