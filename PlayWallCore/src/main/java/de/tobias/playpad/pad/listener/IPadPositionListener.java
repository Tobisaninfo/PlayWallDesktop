package de.tobias.playpad.pad.listener;

import de.tobias.playpad.pad.Pad;
import javafx.beans.value.ChangeListener;
import javafx.util.Duration;

public interface IPadPositionListener extends ChangeListener<Duration> {

	void setPad(Pad pad);

	void setSend(boolean send);

	void stopWaning();

}