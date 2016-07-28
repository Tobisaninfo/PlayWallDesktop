package de.tobias.playpad.viewcontroller;

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
	 * @param videoSettingsTabViewController
	 *            tab
	 */
	public void addTab(SettingsTabViewController videoSettingsTabViewController);

}
