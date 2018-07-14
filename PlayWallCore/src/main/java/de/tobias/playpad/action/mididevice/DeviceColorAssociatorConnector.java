package de.tobias.playpad.action.mididevice;

import de.tobias.playpad.action.feedback.DisplayableFeedbackColor;
import javafx.scene.paint.Color;

public interface DeviceColorAssociatorConnector {

	DisplayableFeedbackColor getDefaultStandardColor();

	DisplayableFeedbackColor getDefaultEventColor();

	DisplayableFeedbackColor[] getColors();

	DisplayableFeedbackColor map(Color color);
}
