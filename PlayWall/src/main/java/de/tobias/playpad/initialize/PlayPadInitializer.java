package de.tobias.playpad.initialize;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.App;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.tobias.playpad.PlayPadImpl;

import java.util.ArrayList;
import java.util.List;

public class PlayPadInitializer implements Runnable {

	public interface Listener {
		void startLoading(int count);

		void startTask(PlayPadInitializeTask task);

		void finishTask(PlayPadInitializeTask task);

		void finishLoading();

		void abortedLoading();
	}

	private List<PlayPadInitializeTask> tasks;

	private PlayPadImpl instance;
	private Listener listener;

	public PlayPadInitializer(PlayPadImpl instance, Listener listener) {
		tasks = new ArrayList<>();
		this.instance = instance;
		this.listener = listener;
	}

	public void submit(PlayPadInitializeTask task) {
		tasks.add(task);
	}

	public void start() {
		Thread thread = new Thread(this);
		thread.setName("PlayPad Initializer Thread");
		thread.start();
	}

	@Override
	public void run() {
		try {
			App app = ApplicationUtils.getApplication();

			listener.startLoading(tasks.size());

			for (PlayPadInitializeTask task : tasks) {
				listener.startTask(task);
				task.run(app, instance);
				listener.finishTask(task);
			}

			listener.finishLoading();
		} catch (PlayPadInitializeAbortException e) {
			listener.abortedLoading();
			Logger.debug("Initialization aborted at task: " + e.getTask().name());
		} catch (Exception e) {
			Logger.error(e);
		}
	}
}
