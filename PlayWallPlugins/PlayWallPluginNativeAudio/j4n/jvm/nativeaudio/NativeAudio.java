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
public class NativeAudio extends system.Object {
    
    //<generated-proxy>
    private static system.Type staticType;
    
    protected NativeAudio(net.sf.jni4net.inj.INJEnv __env, long __handle) {
            super(__env, __handle);
    }
    
    @net.sf.jni4net.attributes.ClrConstructor("()V")
    public NativeAudio() {
            super(((net.sf.jni4net.inj.INJEnv)(null)), 0);
        nativeaudio.NativeAudio.__ctorNativeAudio0(this);
    }
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    private native static void __ctorNativeAudio0(net.sf.jni4net.inj.IClrProxy thiz);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)Z")
    public native boolean load(java.lang.String path);
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void play();
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void pause();
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void stop();
    
    @net.sf.jni4net.attributes.ClrMethod("()D")
    public native double getDuration();
    
    @net.sf.jni4net.attributes.ClrMethod("()D")
    public native double getPosition();
    
    @net.sf.jni4net.attributes.ClrMethod("()Z")
    public native boolean isPlaying();
    
    @net.sf.jni4net.attributes.ClrMethod("(J)V")
    public native void seek(long duration);
    
    @net.sf.jni4net.attributes.ClrMethod("(F)V")
    public native void setVolume(float volume);
    
    @net.sf.jni4net.attributes.ClrMethod("(Z)V")
    public native void setLoop(boolean loop);
    
    @net.sf.jni4net.attributes.ClrMethod("(LSystem/String;)V")
    public native void setDevice(java.lang.String name);
    
    @net.sf.jni4net.attributes.ClrMethod("()[LSystem/String;")
    public native static java.lang.String[] getDevices();
    
    @net.sf.jni4net.attributes.ClrMethod("()V")
    public native void unload();
    
    public static system.Type typeof() {
        return nativeaudio.NativeAudio.staticType;
    }
    
    private static void InitJNI(net.sf.jni4net.inj.INJEnv env, system.Type staticType) {
        nativeaudio.NativeAudio.staticType = staticType;
    }
    //</generated-proxy>
}
