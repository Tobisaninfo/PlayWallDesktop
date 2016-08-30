package de.tobias.playpad.action.mapper;

import java.util.ResourceBundle;

import de.tobias.utils.ui.ContentViewController;

/**
 * Übersicht über die Mapper zu einer Action.s
 * 
 * @author tobias
 * 
 * @since 5.0.0
 *
 */
public abstract class MapperViewController extends ContentViewController {

	public MapperViewController(String name, String path, ResourceBundle localization) {
		super(name, path, localization);
	}

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
