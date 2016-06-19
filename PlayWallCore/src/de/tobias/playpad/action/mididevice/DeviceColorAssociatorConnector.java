package de.tobias.playpad.action.mididevice;

import de.tobias.playpad.action.feedback.DisplayableFeedbackColor;
import javafx.scene.paint.Color;

public interface DeviceColorAssociatorConnector {

	public DisplayableFeedbackColor getDefaultStandardColor();

	public DisplayableFeedbackColor getDefaultEventColor();

	public DisplayableFeedbackColor[] getColors();
	
	public DisplayableFeedbackColor map(Color color);
}
