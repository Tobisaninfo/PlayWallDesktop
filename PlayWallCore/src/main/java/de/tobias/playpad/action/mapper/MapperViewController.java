package de.tobias.playpad.action.mapper;

import de.tobias.utils.nui.NVC;

/**
 * Übersicht über die Mapper zu einer Action.s
 *
 * @author tobias
 * @since 5.0.0
 */
public abstract class MapperViewController extends NVC {

	public abstract void showFeedback();

	public abstract void hideFeedback();

	public abstract Mapper getMapper();

	/**
	 * Zeigt einen Dialog für die Eingabe des Mappers.
	 *
	 * @return <code>true</code> Erfolgreiche Einageb, <code>false</code> Abbruch oder Fehler.
	 */
	public abstract boolean showInputMapperUI();
}
