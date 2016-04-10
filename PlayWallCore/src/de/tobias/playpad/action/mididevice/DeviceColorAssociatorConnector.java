package de.tobias.playpad.action.mididevice;

import de.tobias.playpad.action.feedback.DisplayableFeedbackColor;

public interface DeviceColorAssociatorConnector {

	public DisplayableFeedbackColor getDefaultStandardColor();

	public DisplayableFeedbackColor getDefaultEventColor();

	public DisplayableFeedbackColor[] getColors();
}
