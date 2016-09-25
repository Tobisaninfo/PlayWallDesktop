package de.tobias.playpad;

import de.tobias.playpad.action.ActionConnect;
import de.tobias.playpad.action.mapper.MapperConnect;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.design.DesignConnect;
import de.tobias.playpad.pad.conntent.PadContentRegistry;
import de.tobias.playpad.pad.drag.PadDragMode;
import de.tobias.playpad.registry.ComponentRegistry;
import de.tobias.playpad.registry.DefaultComponentRegistry;
import de.tobias.playpad.registry.DefaultRegistry;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.tigger.TriggerItemConnect;
import de.tobias.playpad.view.main.MainLayoutConnect;

public class RegistryCollectionImpl implements RegistryCollection {

	private Registry<ActionConnect> actionRegistry;
	private AudioRegistry audioHandlerRegistry;
	private Registry<PadDragMode> dragModeRegistry;
	private DefaultRegistry<DesignConnect> layoutRegistry;
	private Registry<MapperConnect> mapperRegistry;
	private PadContentRegistry padContentRegistry;
	private Registry<TriggerItemConnect> triggerItemRegistry;
	private DefaultRegistry<MainLayoutConnect> mainLayoutRegistry;

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
	public Registry<ActionConnect> getActions() {
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
	public DefaultRegistry<DesignConnect> getDesigns() {
		return layoutRegistry;
	}

	@Override
	public Registry<MapperConnect> getMappers() {
		return mapperRegistry;
	}

	@Override
	public PadContentRegistry getPadContents() {
		return padContentRegistry;
	}

	@Override
	public Registry<TriggerItemConnect> getTriggerItems() {
		return triggerItemRegistry;
	}
	
	@Override
	public DefaultRegistry<MainLayoutConnect> getMainLayouts() {
		return mainLayoutRegistry;
	}

}
