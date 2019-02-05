package de.tobias.playpad.plugin;

import de.thecodelabs.plugins.Plugin;

/**
 * Schnittatelle, von der Plugins erben, damit diese alle notwendigen Services unterstützen.
 *
 * @author tobias - s0553746
 */
public interface PlayPadPluginStub extends Plugin {

	Module getModule();

}