package de.tobias.playpad.pad.view;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import javafx.css.PseudoClass;
import javafx.scene.layout.Pane;

/**
 * Zugriff auf eine PadView. Hier sind alle Methoden um mit der GUI für ein Pad zu agieren.
 * 
 * @author tobias
 * 
 * @since 5.1.0
 *
 */
public interface IPadView {

	/**
	 * Gibt die aktuelle Vorschau für den Content eines Pads zurück.
	 * 
	 * @return ContentView
	 */
	public IPadContentView getContentView();

	/**
	 * Setzt die Preview für den PadContent.
	 * 
	 * @param pad
	 *            Pad
	 */
	public void setContentView(Pad pad);

	/**
	 * Gibt den zugehörigen ViewController zu einem Pad zurück.
	 * 
	 * @return ViewController des Pad
	 */
	public IPadViewController getViewController();

	/**
	 * Gibt das oberste GUI Element zurück, welche im MainView verwendet wird.
	 * 
	 * @return root node
	 */
	public Pane getRootNode();

	/**
	 * Schaltet den Design Modus für Drag And Drop ein.
	 * 
	 * @param enable
	 *            true eingeschaltet
	 */
	public void enableDragAndDropDesignMode(boolean enable);

	/**
	 * Zeigt ein BusyView über dem Padview an.
	 * 
	 * @param enable
	 *            true, wird angezeigt
	 */
	public void showBusyView(boolean enable);

	/**
	 * Aktiviert eine Pseudoclass für die View
	 * 
	 * @param playCalss
	 *            Pseudoclass
	 * @param b
	 *            <code>true</code> Aktiv
	 */
	public void pseudoClassState(PseudoClass playCalss, boolean b);

	/**
	 * Setzt den Style für den Root Node der PadView.
	 * 
	 * @param string
	 *            Style
	 */
	public void setStyle(String string);

	/**
	 * Hebt eine Kachel hervor (Beispiel mit einer Animation.
	 * 
	 * @param milliSecounds
	 *            Dauer in Millisekunden
	 */
	public void highlightView(int milliSecounds);

	/**
	 * Aktiviert des Error Labels, damit es angezeigt wird.
	 * 
	 * @param b
	 *            <code>true</code> Sichtbar
	 */
	public void setErrorLabelActive(boolean b);

	/**
	 * Setzt den Fortschritt auf der PlayBar
	 * 
	 * @param value
	 *            [0, 1]
	 */
	public void setPlayBarProgress(double value);

	/**
	 * Setzt die Playbar sichtbar.
	 * 
	 * @param visible
	 *            <code>true</code> Sichtbar, <code>false</code> nicht sichtbar
	 */
	public void setPlaybarVisible(boolean visible);

	/**
	 * Fügt die Standart Elemente der PadView hinzu. Die GUI Element sind Abhängig vom Pad, und welchen Content es hat.
	 * 
	 * @param pad
	 *            Pad
	 */
	public void addDefaultElement(Pad pad);

	/**
	 * Fügt die StyleClasses der PadView hinzu. Die Methode wird vom Controller aufgerufen.
	 */
	public void applyStyleClasses(int index);

	/**
	 * Entfernt die StyleClasses vom PadView. Die Methode wird vom Controller aufgerufen.
	 */
	public void removeStyleClasses();
}
