package de.tobias.playpad.action.mapper;

import de.thecodelabs.midi.mapping.Key;
import de.thecodelabs.utils.ui.NVC;

/**
 * Übersicht über die Mapper zu einer Actions.
 *
 * @author tobias
 * @since 5.0.0
 */
public abstract class MapperViewController extends NVC {

	public abstract void showFeedback();

	public abstract void hideFeedback();

	public abstract Key getKey();

	/**
	 * Zeigt einen Dialog für die Eingabe des Mappers.
	 *
	 * @return <code>true</code> Erfolgreiche Einageb, <code>false</code> Abbruch oder Fehler.
	 */
	public abstract boolean showInputMapperUI();
}
