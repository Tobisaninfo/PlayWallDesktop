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
    public partial class NativeAudio_ {
        
        public static global::java.lang.Class _class {
            get {
                return global::NativeAudio.@__NativeAudio.staticClass;
            }
        }
    }
    #endregion
    
    #region Component Designer generated code 
    [global::net.sf.jni4net.attributes.JavaProxyAttribute(typeof(global::NativeAudio.NativeAudio), typeof(global::NativeAudio.NativeAudio_))]
    [global::net.sf.jni4net.attributes.ClrWrapperAttribute(typeof(global::NativeAudio.NativeAudio), typeof(global::NativeAudio.NativeAudio_))]
    internal sealed partial class @__NativeAudio : global::java.lang.Object {
        
        internal new static global::java.lang.Class staticClass;
        
        private @__NativeAudio(global::net.sf.jni4net.jni.JNIEnv @__env) : 
                base(@__env) {
        }
        
        private static void InitJNI(global::net.sf.jni4net.jni.JNIEnv @__env, java.lang.Class @__class) {
            global::NativeAudio.@__NativeAudio.staticClass = @__class;
        }
        
        private static global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod> @__Init(global::net.sf.jni4net.jni.JNIEnv @__env, global::java.lang.Class @__class) {
            global::System.Type @__type = typeof(__NativeAudio);
            global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod> methods = new global::System.Collections.Generic.List<global::net.sf.jni4net.jni.JNINativeMethod>();
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "load", "load0", "(Ljava/lang/String;)Z"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "play", "play1", "()V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "pause", "pause2", "()V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "stop", "stop3", "()V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "getDuration", "getDuration4", "()D"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "getPosition", "getPosition5", "()D"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "isPlaying", "isPlaying6", "()Z"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "setVolume", "setVolume7", "(F)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "setLoop", "setLoop8", "(Z)V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "unload", "unload9", "()V"));
            methods.Add(global::net.sf.jni4net.jni.JNINativeMethod.Create(@__type, "__ctorNativeAudio0", "__ctorNativeAudio0", "(Lnet/sf/jni4net/inj/IClrProxy;)V"));
            return methods;
        }
        
        private static bool load0(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, global::net.sf.jni4net.utils.JniLocalHandle path) {
            // (Ljava/lang/String;)Z
            // (LSystem/String;)Z
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            bool @__return = default(bool);
            try {
            global::NativeAudio.NativeAudio @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::NativeAudio.NativeAudio>(@__env, @__obj);
            @__return = ((bool)(@__real.load(global::net.sf.jni4net.utils.Convertor.StrongJ2CString(@__env, path))));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
            return @__return;
        }
        
        private static void play1(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()V
            // ()V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::NativeAudio.NativeAudio @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::NativeAudio.NativeAudio>(@__env, @__obj);
            @__real.play();
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void pause2(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()V
            // ()V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::NativeAudio.NativeAudio @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::NativeAudio.NativeAudio>(@__env, @__obj);
            @__real.pause();
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void stop3(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()V
            // ()V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::NativeAudio.NativeAudio @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::NativeAudio.NativeAudio>(@__env, @__obj);
            @__real.stop();
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static double getDuration4(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()D
            // ()D
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            double @__return = default(double);
            try {
            global::NativeAudio.NativeAudio @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::NativeAudio.NativeAudio>(@__env, @__obj);
            @__return = ((double)(@__real.getDuration()));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
            return @__return;
        }
        
        private static double getPosition5(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()D
            // ()D
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            double @__return = default(double);
            try {
            global::NativeAudio.NativeAudio @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::NativeAudio.NativeAudio>(@__env, @__obj);
            @__return = ((double)(@__real.getPosition()));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
            return @__return;
        }
        
        private static bool isPlaying6(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()Z
            // ()Z
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            bool @__return = default(bool);
            try {
            global::NativeAudio.NativeAudio @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::NativeAudio.NativeAudio>(@__env, @__obj);
            @__return = ((bool)(@__real.isPlaying()));
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
            return @__return;
        }
        
        private static void setVolume7(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, float volume) {
            // (F)V
            // (F)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::NativeAudio.NativeAudio @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::NativeAudio.NativeAudio>(@__env, @__obj);
            @__real.setVolume(volume);
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void setLoop8(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj, bool loop) {
            // (Z)V
            // (Z)V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::NativeAudio.NativeAudio @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::NativeAudio.NativeAudio>(@__env, @__obj);
            @__real.setLoop(loop);
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void unload9(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()V
            // ()V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::NativeAudio.NativeAudio @__real = global::net.sf.jni4net.utils.Convertor.StrongJp2C<global::NativeAudio.NativeAudio>(@__env, @__obj);
            @__real.unload();
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        private static void @__ctorNativeAudio0(global::System.IntPtr @__envp, global::net.sf.jni4net.utils.JniLocalHandle @__class, global::net.sf.jni4net.utils.JniLocalHandle @__obj) {
            // ()V
            // ()V
            global::net.sf.jni4net.jni.JNIEnv @__env = global::net.sf.jni4net.jni.JNIEnv.Wrap(@__envp);
            try {
            global::NativeAudio.NativeAudio @__real = new global::NativeAudio.NativeAudio();
            global::net.sf.jni4net.utils.Convertor.InitProxy(@__env, @__obj, @__real);
            }catch (global::System.Exception __ex){@__env.ThrowExisting(__ex);}
        }
        
        new internal sealed class ContructionHelper : global::net.sf.jni4net.utils.IConstructionHelper {
            
            public global::net.sf.jni4net.jni.IJvmProxy CreateProxy(global::net.sf.jni4net.jni.JNIEnv @__env) {
                return new global::NativeAudio.@__NativeAudio(@__env);
            }
        }
    }
    #endregion
}
