package de.tobias.playpad.pad.viewcontroller;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.Playlistable;
import de.tobias.playpad.pad.listener.IPadPositionListener;
import de.tobias.playpad.pad.view.IPadView;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.util.Duration;

/**
 * Schnittstellen um mit einem PadViewController zu kommunizieren.
 *
 * @author tobias
 * @since 5.1.0
 */
public abstract class AbstractPadViewController {

	/**
	 * Gibt das Pad zurück, welches er verwaltet. (Das Datenmodel)
	 *
	 * @return Pad
	 */
	public abstract Pad getPad();

	/**
	 * Gibt die View des Controllers zurück.
	 *
	 * @return View
	 */
	public abstract IPadView getView();

	/**
	 * Setzt ein Pad für ein View. Hier werden die Datein mittels ViewController der View bekannt gemacht.
	 *
	 * @param pad Neues Pad
	 */
	public abstract void setupPad(Pad pad);

	/**
	 * Entfertn des Verbundene Pad von der View.
	 */
	public abstract void removePad();

	public abstract void updateTimeLabel();

	public abstract void updateButtonDisable();

	public abstract IPadPositionListener getPadPositionListener();

	public abstract ChangeListener<Duration> getPadDurationListener();

	public void updatePlaylistLabelBinding(Pad pad) {
		if (pad.getContent() instanceof Playlistable) {
			final Playlistable content = (Playlistable) pad.getContent();
			getView().getPlaylistLabel().textProperty().bind(Bindings.createStringBinding(() -> {
				final int currentPlayingMediaIndex = content.getCurrentPlayingMediaIndex();
				final int totalCount = pad.getPaths().size();

				if (currentPlayingMediaIndex < 0) {
					return "- / " + totalCount;
				} else {
					return (currentPlayingMediaIndex + 1) + " / " + totalCount;
				}
			}, content.currentPlayingMediaIndexProperty(), pad.getPaths()));
		} else {
			getView().getPlaylistLabel().textProperty().unbind();
			getView().getPlaylistLabel().setText("");
		}
	}
}
