package de.tobias.playpad.viewcontroller.option;

/**
 * Schnittstelle um die GlobalSettingsView zu ändern.
 *
 * @author tobias
 * @since 5.1.0
 */
public interface IGlobalSettingsViewController {

	/**
	 * Fügt en Tab hinzu.
	 *
	 * @param globalSettingsTabViewController tab
	 */
	void addTab(GlobalSettingsTabViewController globalSettingsTabViewController);

}
