package de.tobias.playpad.viewcontroller.option;

/**
 * Schnittstelle um die GlobalSettingsView zu ändern.
 *
 * @author tobias
 * @since 5.1.0
 */
public interface IProjectSettingsViewController {

	/**
	 * Fügt en Tab hinzu.
	 *
	 * @param projectSettingsTabViewController tab
	 */
	void addTab(ProjectSettingsTabViewController projectSettingsTabViewController);

}
