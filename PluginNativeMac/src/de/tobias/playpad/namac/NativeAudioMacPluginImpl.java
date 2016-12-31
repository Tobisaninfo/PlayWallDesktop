package de.tobias.playpad.namac;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.plugin.Module;
import de.tobias.updater.client.Updatable;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.IOUtils;
import de.tobias.utils.util.OS;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.events.Shutdown;

@PluginImplementation
public class NativeAudioMacPluginImpl implements NativeAudioMacPlugin {

	private static final String ASSETS = "de/tobias/playpad/";

	private static final String NAME = "NativeAudioMac";
	private static final String IDENTIFIER = "de.tobias.playpad.namac.NativeAudioMacPluginImpl";

	private Module module;
	private Updatable updatable;

	@PluginLoaded
	public void onLoaded(NativeAudioMacPlugin plugin) {
		module = new Module(NAME, IDENTIFIER);
		updatable = new NativeAudioMacUpdater();

		try {
			prepareBridging();

			if (OS.isMacOS()) {
				AudioRegistry registry = PlayPadPlugin.getRegistryCollection().getAudioHandlers();
				NativeAudioMacHandlerFactory nativeMac = new NativeAudioMacHandlerFactory("NativeMac");
				nativeMac.setName("NativeMac");
				registry.registerComponent(nativeMac, module);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void prepareBridging() throws IOException {
		App app = ApplicationUtils.getApplication();
		Path resourceFolder = app.getPath(PathType.LIBRARY, "namac");
		if (Files.notExists(resourceFolder)) {
			Files.createDirectories(resourceFolder);
		}

		Path dest = copyResource(resourceFolder, ASSETS, "libNativeAudio.dylib");
		System.load(dest.toString());
	}

	private Path copyResource(Path resourceFolder, String packageName, String file) throws IOException {
		Path dest = resourceFolder.resolve(file);
		IOUtils.copy(getClass().getClassLoader().getResourceAsStream(packageName + file), dest);
		System.out.println("Copied: " + file);
		return dest;
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
