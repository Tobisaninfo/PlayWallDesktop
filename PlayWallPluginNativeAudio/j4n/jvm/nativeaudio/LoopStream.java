// ------------------------------------------------------------------------------
//  <autogenerated>
//      This code was generated by jni4net. See http://jni4net.sourceforge.net/ 
// 
//      Changes to this file may cause incorrect behavior and will be lost if 
//      the code is regenerated.
//  </autogenerated>
// ------------------------------------------------------------------------------

package nativeaudio;

@net.sf.jni4net.attributes.ClrType
public class LoopStream extends system.io.Stream {

	//<generated-proxy>
	private static system.Type staticType;

	protected LoopStream(net.sf.jni4net.inj.INJEnv __env, long __handle) {
		super(__env, __handle);
	}

	@net.sf.jni4net.attributes.ClrConstructor("(LNAudio/Wave/WaveStream;)V")
	public LoopStream(system.io.Stream sourceStream) {
		super(((net.sf.jni4net.inj.INJEnv) (null)), 0);
		nativeaudio.LoopStream.__ctorLoopStream0(this, sourceStream);
	}

	@net.sf.jni4net.attributes.ClrMethod("(Lsystem/io/Stream;)V")
	private native static void __ctorLoopStream0(net.sf.jni4net.inj.IClrProxy thiz, system.io.Stream sourceStream);

	@net.sf.jni4net.attributes.ClrMethod("()Z")
	public native boolean getEnableLooping();

	@net.sf.jni4net.attributes.ClrMethod("(Z)V")
	public native void setEnableLooping(boolean value);

	public static system.Type typeof() {
		return nativeaudio.LoopStream.staticType;
	}

	private static void InitJNI(net.sf.jni4net.inj.INJEnv env, system.Type staticType) {
		nativeaudio.LoopStream.staticType = staticType;
	}
	//</generated-proxy>
}
