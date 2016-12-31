package de.tobias.playpad;

import de.tobias.playpad.action.ActionFactory;
import de.tobias.playpad.action.mapper.MapperFactory;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.design.DesignFactory;
import de.tobias.playpad.pad.conntent.PadContentRegistry;
import de.tobias.playpad.pad.drag.PadDragMode;
import de.tobias.playpad.registry.ComponentRegistry;
import de.tobias.playpad.registry.DefaultComponentRegistry;
import de.tobias.playpad.registry.DefaultRegistry;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.tigger.TriggerItemFactory;
import de.tobias.playpad.view.main.MainLayoutFactory;

public class RegistryCollectionImpl implements RegistryCollection {

	private Registry<ActionFactory> actionRegistry;
	private AudioRegistry audioHandlerRegistry;
	private Registry<PadDragMode> dragModeRegistry;
	private DefaultRegistry<DesignFactory> layoutRegistry;
	private Registry<MapperFactory> mapperRegistry;
	private PadContentRegistry padContentRegistry;
	private Registry<TriggerItemFactory> triggerItemRegistry;
	private DefaultRegistry<MainLayoutFactory> mainLayoutRegistry;

	public RegistryCollectionImpl() {
		actionRegistry = new ComponentRegistry<>("Action");
		audioHandlerRegistry = new AudioRegistry();
		dragModeRegistry = new ComponentRegistry<>("DragMode");
		layoutRegistry = new DefaultComponentRegistry<>("Layout");
		mapperRegistry = new ComponentRegistry<>("Mapper");
		padContentRegistry = new PadContentRegistry("PadContent");
		triggerItemRegistry = new ComponentRegistry<>("Trigger");
		mainLayoutRegistry = new DefaultComponentRegistry<>("MainLayout");
	}

	@Override
	public Registry<ActionFactory> getActions() {
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
	public DefaultRegistry<DesignFactory> getDesigns() {
		return layoutRegistry;
	}

	@Override
	public Registry<MapperFactory> getMappers() {
		return mapperRegistry;
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
