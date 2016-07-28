package de.tobias.playpad.update;

public enum UpdateChannel {

	STABLE("stable"),
	BETA("beta");

	private String name;

	private UpdateChannel(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
