package de.tobias.playpad.audio.mac;

public class AVAudioPlayerBridge {

	private long nativePointer;

	private long getNativePointer() {
		return nativePointer;
	}

	private void setNativePointer(long nativePointer) {
		this.nativePointer = nativePointer;
	}


	static {
		System.out.println("static init start");
		initialize();
		System.out.println("static init end");
	}

	private static native void initialize();

	public AVAudioPlayerBridge() {
		init();
	}

	private native void init();

	public native void play();

	public native void pause();

	public native void stop();

	public native void seek(double duration);

	public native void setLoop(boolean loop);

	public native double getVolume();

	public native void setVolume(double volume);

	public native boolean load(String path);

	public native void dispose();

	public native double getDuration();

	public native double getPosition();

	public native void setRate(double rate);

	public void onPeakMeter(float left, float right) {
		if (delegate != null) {
			delegate.onPeakMeter(this, left, right);
		}
	}

	public void onPositionChanged(double position) {
		if (delegate != null) {
			delegate.onPositionChanged(this, position);
		}
	}

	public void onFinish() {
		if (delegate != null) {
			delegate.onFinish(this);
		}
	}

	private static NativeAudioDelegate delegate;

	public static void setDelegate(NativeAudioDelegate delegate) {
		AVAudioPlayerBridge.delegate = delegate;
	}

	public interface NativeAudioDelegate {
		void onFinish(AVAudioPlayerBridge bridge);

		void onPeakMeter(AVAudioPlayerBridge bridge, float left, float right);

		void onPositionChanged(AVAudioPlayerBridge bridge, double position);
	}
}
