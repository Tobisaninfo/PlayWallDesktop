package de.tobias.playpad.viewcontroller.option;

/**
 * Schnittstelle um die ProfileSettingsView zu ändern.
 * 
 * @author tobias
 *
 * @since 5.0.0
 */
public interface ISettingsViewController {

	/**
	 * Fügt en Tab hinzu.
	 * 
	 * @param profileSettingsTabViewController
	 *            tab
	 */
	public void addTab(SettingsTabViewController profileSettingsTabViewController);

}
