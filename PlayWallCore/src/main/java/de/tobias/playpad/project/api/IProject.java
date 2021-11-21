package de.tobias.playpad.project.api;

import java.util.List;

public interface IProject {
	List<? extends IPage> getPages();

	IPage getPage(int index);

	IProjectSettings getSettings();
}
