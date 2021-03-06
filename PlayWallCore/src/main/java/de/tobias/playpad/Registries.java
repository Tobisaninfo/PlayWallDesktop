package de.tobias.playpad;

import de.tobias.playpad.action.ActionProvider;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.pad.content.PadContentRegistry;
import de.tobias.playpad.pad.drag.PadDragMode;
import de.tobias.playpad.registry.DefaultRegistry;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.tigger.TriggerItemFactory;
import de.tobias.playpad.view.main.MainLayoutFactory;

/**
 * Schnittstelle für die einzelen Registry. Hier sind alle Registries gesammelt, damit Komponenten registriert werden können. Die
 * eigentliche Implementierung ist im Hauptprogramm.
 *
 * @author tobias
 * @since 5.1.0
 */
public interface Registries {

	Registry<ActionProvider> getActions();

	AudioRegistry getAudioHandlers();

	Registry<PadDragMode> getDragModes();

	PadContentRegistry getPadContents();

	Registry<TriggerItemFactory> getTriggerItems();

	DefaultRegistry<MainLayoutFactory> getMainLayouts();
}