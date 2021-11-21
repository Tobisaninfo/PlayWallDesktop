package de.tobias.playpad.project.api;

import java.util.Collection;
import java.util.UUID;

public interface IPage {
	UUID getId();

	int getPosition();

	String getName();

	IPad getPad(int position);

	IPad getPad(int x, int y);

	Collection<? extends IPad> getPads();

	default int getPadPosition(int x, int y, IProjectSettings settings) {
		return y * settings.getColumns() + x;
	}
}
