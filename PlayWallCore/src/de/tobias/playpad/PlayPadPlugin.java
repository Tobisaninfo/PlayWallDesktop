package de.tobias.playpad;


public final class PlayPadPlugin {

	private static PlayPad implementation;
	
	public static PlayPad getImplementation() {
		return implementation;
	}

	protected static void setImplementation(PlayPad playPadMain) {
		implementation = playPadMain;
	}
}
