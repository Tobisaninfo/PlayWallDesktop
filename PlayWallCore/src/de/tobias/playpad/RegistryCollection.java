package de.tobias.playpad;

import de.tobias.playpad.action.ActionConnect;
import de.tobias.playpad.action.mapper.MapperConnect;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.design.DesignConnect;
import de.tobias.playpad.pad.PadContentRegistry;
import de.tobias.playpad.pad.drag.PadDragMode;
import de.tobias.playpad.registry.DefaultRegistry;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.tigger.TriggerItemConnect;

/**
 * Schnittstelle für die einzelen Registry. Hier sind alle Registries gesammelt, damit Komponenten registriert werden können. Die
 * eigentliche Implementierung ist im Hauptprogramm.
 * 
 * @author tobias
 *
 * @since 5.1.0
 */
public interface RegistryCollection {

	public Registry<ActionConnect> getActions();

	public AudioRegistry getAudioHandlers();

	public Registry<PadDragMode> getDragModes();

	public DefaultRegistry<DesignConnect> getDesigns();

	public Registry<MapperConnect> getMappers();

	public PadContentRegistry getPadContents();

	public Registry<TriggerItemConnect> getTriggerItems();

}