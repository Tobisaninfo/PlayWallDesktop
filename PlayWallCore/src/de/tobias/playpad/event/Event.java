package de.tobias.playpad.event;

public class Event {

	private final long time;
	private boolean consume;

	public Event() {
		time = System.currentTimeMillis();
		consume = false;
	}

	public long getTime() {
		return time;
	}

	public boolean isConsume() {
		return consume;
	}

	public void setConsume(boolean consume) {
		this.consume = consume;
	}
}
