package de.tobias.playpad;

public class NativeAudio {

	public static native void initialize();

	public static native void play(int id);

	public static native void pause(int id);

	public static native void stop(int id);

	public static native void seek(int id, double duration);

	public static native void setLoop(int id, boolean loop);

	public static native double getVolume(int id);

	public static native void setVolume(int id, double volume);

	public static native boolean load(int id, String path);

	public static native void dispose(int id);

	public static native double getDuration(int id);

	public static native double getPosition(int id);

	public static void onPeakMeter(int id, float left, float right) {
		if (delegate != null) {
			delegate.onPeakMeter(id, left, right);
		}
	}

	public static void onPositionChanged(int id, double position) {
		if (delegate != null) {
			delegate.onPositionChanged(id, position);
		}
	}

	public static void onFinish(int id) {
		if (delegate != null) {
			delegate.onFinish(id);
		}
	}

	private static NativeAudioDelegate delegate;

	public static void setDelegate(NativeAudioDelegate delegate) {
		NativeAudio.delegate = delegate;
	}

	public interface NativeAudioDelegate {
		void onFinish(int id);

		void onPeakMeter(int id, float left, float right);

		void onPositionChanged(int id, double position);
	}
}
