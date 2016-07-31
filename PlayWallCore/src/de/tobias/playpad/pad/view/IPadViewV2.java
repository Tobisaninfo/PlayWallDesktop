package de.tobias.playpad.pad.view;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.viewcontroller.IPadViewControllerV2;
import javafx.css.PseudoClass;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;

/**
 * Zugriff auf eine PadView. Hier sind alle Methoden um mit der GUI für ein Pad zu agieren.
 * 
 * @author tobias
 * 
 * @since 5.1.0
 *
 */
public interface IPadViewV2 {

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
	public IPadViewControllerV2 getViewController();

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

	public void pseudoClassState(PseudoClass playCalss, boolean b);

	public void setStyle(String string);

	public void setErrorLabelActive(boolean b);

	// GUI Elemente
	@Deprecated
	public ProgressBar getPlayBar();

	public void addDefaultButton(Pad pad);

	public void applyStyleClasses();

	public void removeStyleClasses();
}
