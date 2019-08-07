@echo off
if not exist target mkdir target
if not exist target\classes mkdir target\classes


echo compile classes
javac -nowarn -d target\classes -sourcepath jvm -cp "d:\programmieren\git-java\playwall\playwallnativewin\j4n\jni4net.j-0.8.8.0.jar"; "jvm\nativeaudio\LoopStream.java" "jvm\nativeaudio\NativeAudio.java" 
IF %ERRORLEVEL% NEQ 0 goto end


echo NativeAudio.j4n.jar 
jar cvf NativeAudio.j4n.jar  -C target\classes "nativeaudio\LoopStream.class"  -C target\classes "nativeaudio\NativeAudio.class"  > nul 
IF %ERRORLEVEL% NEQ 0 goto end


echo NativeAudio.j4n.dll 
csc /nologo /warn:0 /t:library /out:NativeAudio.j4n.dll /recurse:clr\*.cs  /reference:"D:\Programmieren\Git-Java\PlayWall\PlayWallNativeWin\j4n\NativeAudio.dll" /reference:"D:\Programmieren\Git-Java\PlayWall\PlayWallNativeWin\j4n\NAudio.dll" /reference:"D:\Programmieren\Git-Java\PlayWall\PlayWallNativeWin\j4n\jni4net.n-0.8.8.0.dll"
IF %ERRORLEVEL% NEQ 0 goto end


:end
