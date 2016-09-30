package de.tobias.playpad.nawin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.nawin.audio.NativeAudioWinHandlerConnect;
import de.tobias.playpad.nawin.audio.NativeAudioWinPlugin;
import de.tobias.playpad.plugin.Module;
import de.tobias.updater.client.Updatable;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.IOUtils;
import de.tobias.utils.util.OS;
import net.sf.jni4net.Bridge;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.events.Shutdown;

@PluginImplementation
public class NativeAudioWinPluginImpl implements NativeAudioWinPlugin {

	private static final String ASSETS = "de/tobias/playpad/nawin/assets/";
	
	private static final String NAME = "NativeAudioWin";
	private static final String IDENTIFIER = "de.tobias.playpad.nawin.NativeAudioWinPluginImpl";
	
	private Module module;
	private Updatable updatable;
	
	@PluginLoaded
	public void onLoaded(NativeAudioWinPlugin plugin) {
		module = new Module(NAME, IDENTIFIER);
		updatable = new NativeAudioWinUpdater();
		
		try {
			prepareBridging();
			bridgeCsharp();

			if (OS.isWindows()) {
				AudioRegistry registry = PlayPadPlugin.getRegistryCollection().getAudioHandlers();
				registry.registerComponent(new NativeAudioWinHandlerConnect(), "NativeWin", module);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void prepareBridging() throws IOException {
		App app = ApplicationUtils.getApplication();
		Path resourceFolder = app.getPath(PathType.LIBRARY, "nawin");
		if (Files.notExists(resourceFolder)) {
			Files.createDirectories(resourceFolder);
		}

		copyResource(resourceFolder, ASSETS, "jni4net.j-0.8.8.0.jar");
		copyResource(resourceFolder, ASSETS, "jni4net.n-0.8.8.0.dll");
		copyResource(resourceFolder, ASSETS, "jni4net.n.w32.v40-0.8.8.0.dll");
		copyResource(resourceFolder, ASSETS, "jni4net.n.w64.v40-0.8.8.0.dll");
		copyResource(resourceFolder, ASSETS, "NativeAudio.dll");
		copyResource(resourceFolder, ASSETS, "NativeAudio.j4n.dll");
		copyResource(resourceFolder, ASSETS, "NativeAudio.j4n.jar");
		copyResource(resourceFolder, ASSETS, "NAudio.dll");
	}

	private void copyResource(Path resourceFolder, String packageName, String file) throws IOException {
		IOUtils.copy(getClass().getClassLoader().getResourceAsStream(packageName + file), resourceFolder.resolve(file));
		System.out.println("Copied: " + file);
	}

	private void bridgeCsharp() throws IOException {
		Bridge.setVerbose(true);
		Bridge.init();

		App app = ApplicationUtils.getApplication();
		Path resourceFolder = app.getPath(PathType.LIBRARY, "nawin");

		Bridge.LoadAndRegisterAssemblyFrom(resourceFolder.resolve("NativeAudio.j4n.dll").toFile());
	}

	@Shutdown
	public void onShutdown() {

	}
	
	@Override
	public Module getModule() {
		return module;
	}
	
	@Override
	public Updatable getUpdatable() {
		return updatable;
	}
}
