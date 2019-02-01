package de.tobias.playpad.plugin;

import de.thecodelabs.plugins.Plugin;
import de.tobias.updater.client.Updatable;

/**
 * Schnittatelle, von der Plugins erben, damit diese alle notwendigen Services unterstützen.
 *
 * @author tobias - s0553746
 */
public interface AdvancedPlugin extends Plugin {

	Module getModule();

	Updatable getUpdatable();
}
