package de.tobias.playpad.plugin;

import de.thecodelabs.plugins.Plugin;

/**
 * Interface for plugins, to define own modules in playpad for project consistency.
 *
 * @author tobias
 * @version 6.2.0
 */
public interface PlayPadPluginStub extends Plugin {

	Module getModule();
}