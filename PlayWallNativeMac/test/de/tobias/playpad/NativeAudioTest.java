package de.tobias.playpad;

public class NativeAudioTest {
	
	public static void main(String[] args) {
		System.load("/Users/tobias/Documents/Programmieren/Java/git/PlayWall/PlayWallNative/libNativeAudio.dylib");

		NativeAudio.load(0, "/Users/tobias/Downloads/03%20Hymn%20For%20The%20Weekend.mp3.wav");
		System.out.println(NativeAudio.getDuration(0));
		NativeAudio.play(0);
		
		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
