package de.tobias.playpad.initialize;

public class PlayPadInitializeAbortException extends RuntimeException {

	private PlayPadInitializeTask task;

	public PlayPadInitializeAbortException(PlayPadInitializeTask task) {
		this.task = task;
	}

	public PlayPadInitializeTask getTask() {
		return task;
	}
}
