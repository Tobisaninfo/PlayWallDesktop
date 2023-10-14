package de.tobias.playpad.viewcontroller.main;

import de.thecodelabs.logger.Logger;
import de.tobias.playpad.PlayPadPlugin;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AutosaveRunner implements Runnable {
	private final MainViewController mainViewController;

	public AutosaveRunner(MainViewController mainViewController) {
		this.mainViewController = mainViewController;
	}

	@Override
	public void run() {
		long lastSaveTime = System.currentTimeMillis();
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		while (!Thread.interrupted()) {
			final long currentMillis = System.currentTimeMillis();

			// autosave interval may be changed by user in global settings, therefore the current setting needs to be fetched every time
			if (currentMillis > lastSaveTime + getAutosaveIntervalInMillis()) {
				lastSaveTime = currentMillis;

				if (PlayPadPlugin.getInstance().getGlobalSettings().isEnableAutosave()) {
					Logger.debug("Performing autosave...");
					mainViewController.save();

					long nextSaveTime = currentMillis + getAutosaveIntervalInMillis();
					Logger.debug("Autosave done. Next predicted autosave: " + dateFormat.format(new Date(nextSaveTime)));
				}
			}

			try {
				//noinspection BusyWait
				Thread.sleep(10 * 1000L);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private long getAutosaveIntervalInMillis() {
		return PlayPadPlugin.getInstance().getGlobalSettings().getAutosaveIntervalInMinutes() * 60 * 1000L;
	}
}
