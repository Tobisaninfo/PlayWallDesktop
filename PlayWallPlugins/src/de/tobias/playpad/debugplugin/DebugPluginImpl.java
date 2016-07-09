package de.tobias.playpad.debugplugin;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.PluginLoaded;
import net.xeoh.plugins.base.annotations.events.Shutdown;

@PluginImplementation
public class DebugPluginImpl implements DebugPlugin {

	public DebugPluginImpl() {
		
	}

	@PluginLoaded
	public void onInit(DebugPlugin plugin) {
		System.out.println("INIT");
	}

	@Shutdown
	public void onShutdown() {
		System.out.println("SHUTDOWN");
	}
}
