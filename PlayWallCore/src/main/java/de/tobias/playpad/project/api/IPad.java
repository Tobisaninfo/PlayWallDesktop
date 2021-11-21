package de.tobias.playpad.project.api;

import de.tobias.playpad.pad.PadStatus;

import java.util.UUID;

public interface IPad {
	UUID getUuid();

	String getName();

	PadStatus getStatus();

	int getPositionReadable();
}
