//------------------------------------------------------------------------------
// <auto-generated>
//     This code was generated by jni4net. See http://jni4net.sourceforge.net/ 
//     Runtime Version:4.0.30319.42000
//
//     Changes to this file may cause incorrect behavior and will be lost if
//     the code is regenerated.
// </auto-generated>
//------------------------------------------------------------------------------

namespace NativeAudio {
    
    
    #region Component Designer generated code 
    public partial class LoopStream_ {
        
        public static global::java.lang.Class _class {
            get {
                return global::NativeAudio.@__LoopStream.staticClass;
            }
        }
    }
    #endregion
    
    #region Component Designer generated code 
    [global::net.sf.jni4net.attributes.JavaProxyAttribute(typeof(global::NativeAudio.LoopStream), typeof(global::NativeAudio.LoopStream_))]
    [global::net.sf.jni4net.attributes.ClrWrapperAttribute(typeof(global::NativeAudio.LoopStream), typeof(global::NativeAudio.LoopStream_))]
    internal sealed partial class @__LoopStream : global::java.lang.Object {
        
        internal new static global::java.lang.Class staticClass;
        
        private @__LoopStream(global::net.sf.jni4net.jni.JNIEnv @__env) : 
                base(@__env) {
        }
        
        private static void InitJNI(global::net.sf.jni4net.jni.JNIEnv @__env, java.lang.Class @__class) {
            global::NativeAudio.@__LoopStream.staticClass = @__class;
        }
        
        private static global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod> @__Init(global::net.sf.jni4net.jni.JNIEnv @__env, global::java.lang.Class @__class) {
            global::System.Type @__type = typeof(__LoopStream);
            global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod> methods = new global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod>();
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "getEnableLooping", "EnableLooping0", "()Z"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "setEnableLooping", "EnableLooping1", "(Z)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "__ctorLoopStream0", "__ctorLoopStream0", "(Lnet/sf/jni4net/inj/IClrProxy;Lsystem/io/Stream;)V"));
            return methods;
        }
        
        private static bool EnableLooping0(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()Z
            // ()Z
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            bool @__return = default(bool);
            try {
            global::NativeAudio.LoopStream @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::NativeAudio.LoopStream>(@__env, @__obj);
            @__return = ((bool)(@__real.EnableLooping));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
            return @__return;
        }
        
        private static void EnableLooping1(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, bool value) {
            // (Z)V
            // (Z)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::NativeAudio.LoopStream @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::NativeAudio.LoopStream>(@__env, @__obj);
            @__real.EnableLooping = value;
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void @__ctorLoopStream0(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__class, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle sourceStream) {
            // (Lsystem/io/Stream;)V
            // (LNAudio/Wave/WaveStream;)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::NativeAudio.LoopStream @__real = new global::NativeAudio.LoopStream(global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::NAudio.Wave.WaveStream>(@__env, sourceStream));
            global::net.sf.jni4net.utils.Convertor.InitProxy(@__env, @__obj, @__real);
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        new internal sealed class ContructionHelper : global::net.sf.jni4net.utils.IConstructionHelper {
            
            public global::net.sf.jni4net.jni.IJvmProxy CreateProxy(global::net.sf.jni4net.jni.JNIEnv @__env) {
                return new global::NativeAudio.@__LoopStream(@__env);
            }
        }
    }
    #endregion
}
