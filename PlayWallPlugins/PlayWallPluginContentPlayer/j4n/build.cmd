@echo off
if not exist target mkdir target
if not exist target\classes mkdir target\classes


echo compile classes
javac -nowarn -d target\classes -sourcepath jvm -cp "c:\users\tobias\ideaprojects\playwalldesktop\playwallplugins\playwallplugincontentplayer\j4n\jni4net.j-0.8.8.0.jar"; "jvm\nativecontentplayerwindows\ContentPlayerStopListener.java" "jvm\nativecontentplayerwindows\ContentPlayerStopListener_.java" "jvm\nativecontentplayerwindows\Zone.java" "jvm\nativecontentplayerwindows\ContentPlayer.java" "jvm\nativecontentplayerwindows\ContentPlayerWindow.java" 
IF %ERRORLEVEL% NEQ 0 goto end


echo NativeContentPlayerWindows.j4n.jar 
jar cvf NativeContentPlayerWindows.j4n.jar  -C target\classes "nativecontentplayerwindows\ContentPlayerStopListener.class"  -C target\classes "nativecontentplayerwindows\ContentPlayerStopListener_.class"  -C target\classes "nativecontentplayerwindows\__ContentPlayerStopListener.class"  -C target\classes "nativecontentplayerwindows\Zone.class"  -C target\classes "nativecontentplayerwindows\ContentPlayer.class"  -C target\classes "nativecontentplayerwindows\ContentPlayerWindow.class"  > nul 
IF %ERRORLEVEL% NEQ 0 goto end


echo NativeContentPlayerWindows.j4n.dll 
csc /nologo /warn:0 /t:library /out:NativeContentPlayerWindows.j4n.dll /recurse:clr\*.cs  /reference:"C:\Users\tobias\IdeaProjects\PlayWallDesktop\PlayWallPlugins\PlayWallPluginContentPlayer\j4n\NativeContentPlayerWindows.dll" /reference:"C:\Users\tobias\IdeaProjects\PlayWallDesktop\PlayWallPlugins\PlayWallPluginContentPlayer\j4n\jni4net.n-0.8.8.0.dll"
IF %ERRORLEVEL% NEQ 0 goto end


:end
