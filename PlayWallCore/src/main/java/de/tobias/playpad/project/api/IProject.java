package de.tobias.playpad.project.api;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.page.PadIndex;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IProject {
	IProjectSettings getSettings();

	Pad getPad(int x, int y, int page);

	Pad getPad(PadIndex index);

	Pad getPad(UUID uuid);

	Collection<? extends IPad> getPads();

	IPage getPage(int index);
	
	IPage getPage(UUID uuid);

	List<? extends IPage> getPages();
}
