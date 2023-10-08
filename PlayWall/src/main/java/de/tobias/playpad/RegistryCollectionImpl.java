package de.tobias.playpad;

import de.tobias.playpad.action.ActionProvider;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.pad.content.PadContentRegistry;
import de.tobias.playpad.pad.drag.PadDragMode;
import de.tobias.playpad.registry.ComponentRegistry;
import de.tobias.playpad.registry.DefaultComponentRegistry;
import de.tobias.playpad.registry.DefaultRegistry;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.tigger.TriggerItemFactory;
import de.tobias.playpad.view.main.MainLayoutFactory;

public class RegistryCollectionImpl implements Registries {

	private final Registry<ActionProvider> actionRegistry;
	private final AudioRegistry audioHandlerRegistry;
	private final Registry<PadDragMode> dragModeRegistry;
	private final PadContentRegistry padContentRegistry;
	private final Registry<TriggerItemFactory> triggerItemRegistry;
	private final DefaultRegistry<MainLayoutFactory> mainLayoutRegistry;

	public RegistryCollectionImpl() {
		actionRegistry = new ComponentRegistry<>("Action");
		audioHandlerRegistry = new AudioRegistry();
		dragModeRegistry = new ComponentRegistry<>("DragMode");
		padContentRegistry = new PadContentRegistry("PadContent");
		triggerItemRegistry = new ComponentRegistry<>("Trigger");
		mainLayoutRegistry = new DefaultComponentRegistry<>("MainLayout");
	}

	@Override
	public Registry<ActionProvider> getActions() {
		return actionRegistry;
	}

	@Override
	public AudioRegistry getAudioHandlers() {
		return audioHandlerRegistry;
	}

	@Override
	public Registry<PadDragMode> getDragModes() {
		return dragModeRegistry;
	}

	@Override
	public PadContentRegistry getPadContents() {
		return padContentRegistry;
	}

	@Override
	public Registry<TriggerItemFactory> getTriggerItems() {
		return triggerItemRegistry;
	}

	@Override
	public DefaultRegistry<MainLayoutFactory> getMainLayouts() {
		return mainLayoutRegistry;
	}

}
