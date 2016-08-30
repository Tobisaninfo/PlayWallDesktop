package de.tobias.playpad.pad.viewcontroller;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.listener.IPadPositionListener;
import de.tobias.playpad.pad.view.IPadView;
import javafx.beans.value.ChangeListener;
import javafx.util.Duration;

/**
 * Schnittstellen um mit einem PadViewController zu kommunizieren.
 * 
 * @author tobias
 *
 * @since 5.1.0
 */
public interface IPadViewController {

	/**
	 * Gibt das Pad zurück, welches er verwaltet. (Das Datenmodel)
	 * 
	 * @return Pad
	 */
	public Pad getPad();

	/**
	 * Gibt die View des Controllers zurück.
	 * 
	 * @return View
	 */
	public IPadView getView();

	/**
	 * Setzt ein Pad für ein View. Hier werden die Datein mittels ViewController der View bekannt gemacht.
	 * 
	 * @param pad
	 *            Neues Pad
	 */
	public void setupPad(Pad pad);

	/**
	 * Entfertn des Verbundene Pad von der View.
	 */
	public void removePad();

	public void updateTimeLabel();

	public void updateButtonDisable();

	public IPadPositionListener getPadPositionListener();

	public ChangeListener<Duration> getPadDurationListener();
}
