package de.tobias.playpad.server.sync.listener.upstream;

import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.pad.TimeMode;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Change;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import javafx.beans.value.ChangeListener;
import javafx.util.Duration;

public class PadSettingsUpdateListener {

	private PadSettings padSettings;

	private ChangeListener<Number> volumeListener;
	private ChangeListener<Boolean> loopListener;
	private ChangeListener<TimeMode> timeModeListener;
	private ChangeListener<Duration> warningListener;

	public PadSettingsUpdateListener(PadSettings padSettings) {
		this.padSettings = padSettings;

		volumeListener = (observable, oldValue, newValue) -> {
			Change change = new Change(PropertyDef.PAD_SETTINGS_VOLUME, newValue, padSettings);
			CommandManager.execute(Commands.PAD_SETTINGS_UPDATE, padSettings.getPad().getProject().getProjectReference(), change);
		};

		loopListener = (observable, oldValue, newValue) -> {
			Change change = new Change(PropertyDef.PAD_SETTINGS_LOOP, newValue, padSettings);
			CommandManager.execute(Commands.PAD_SETTINGS_UPDATE, padSettings.getPad().getProject().getProjectReference(), change);
		};

		timeModeListener = (observable, oldValue, newValue) -> {
			Change change = new Change(PropertyDef.PAD_SETTINGS_TIME_MODE, newValue, padSettings);
			CommandManager.execute(Commands.PAD_SETTINGS_UPDATE, padSettings.getPad().getProject().getProjectReference(), change);
		};

		warningListener = (observable, oldValue, newValue) -> {
			Change change = new Change(PropertyDef.PAD_SETTINGS_WARNING, newValue.toMillis(), padSettings);
			CommandManager.execute(Commands.PAD_SETTINGS_UPDATE, padSettings.getPad().getProject().getProjectReference(), change);
		};
	}

	private boolean added;

	public void addListener() {
		if (!added) {
			added = true;
			padSettings.volumeProperty().addListener(volumeListener);
			padSettings.loopProperty().addListener(loopListener);
			padSettings.timeModeProperty().addListener(timeModeListener);
			padSettings.warningProperty().addListener(warningListener);
		}
	}

	public void removeListener() {
		added = false;
		padSettings.volumeProperty().removeListener(volumeListener);
		padSettings.loopProperty().removeListener(loopListener);
		padSettings.timeModeProperty().removeListener(timeModeListener);
		padSettings.warningProperty().removeListener(warningListener);
	}

}
