package de.tobias.playpad.plugin;

import de.tobias.updater.client.Updatable;

/**
 * Schnittatelle, von der Plugins erben, damit diese alle notwendigen Services unterstützen.
 *
 * @author tobias - s0553746
 */
public interface AdvancedPlugin extends net.xeoh.plugins.base.Plugin {

	Module getModule();

	Updatable getUpdatable();
}
