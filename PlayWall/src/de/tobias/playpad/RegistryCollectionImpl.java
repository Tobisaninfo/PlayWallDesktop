package de.tobias.playpad;

import de.tobias.playpad.action.ActionConnect;
import de.tobias.playpad.action.mapper.MapperConnect;
import de.tobias.playpad.audio.AudioRegistry;
import de.tobias.playpad.design.DesignConnect;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.pad.drag.PadDragMode;
import de.tobias.playpad.registry.ComponentRegistry;
import de.tobias.playpad.registry.DefaultComponentRegistry;
import de.tobias.playpad.registry.DefaultRegistry;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.tigger.TriggerItemConnect;

public class RegistryCollectionImpl implements RegistryCollection {

	private Registry<ActionConnect> actionRegistry;
	private AudioRegistry audioHandlerRegistry;
	private Registry<PadDragMode> dragModeRegistry;
	private DefaultRegistry<DesignConnect> layoutRegistry;
	private Registry<MapperConnect> mapperRegistry;
	private Registry<PadContentConnect> padContentRegistry;
	private Registry<TriggerItemConnect> triggerItemRegistry;

	public RegistryCollectionImpl() {
		actionRegistry = new ComponentRegistry<>("Action");
		audioHandlerRegistry = new AudioRegistry();
		dragModeRegistry = new ComponentRegistry<>("DragMode");
		layoutRegistry = new DefaultComponentRegistry<>("Layout");
		mapperRegistry = new ComponentRegistry<>("Mapper");
		padContentRegistry = new ComponentRegistry<>("PadContent");
		triggerItemRegistry = new ComponentRegistry<>("Trigger");
	}

	/* (non-Javadoc)
	 * @see de.tobias.playpad.RegistryCollection#getActionRegistry()
	 */
	@Override
	public Registry<ActionConnect> getActions() {
		return actionRegistry;
	}

	/* (non-Javadoc)
	 * @see de.tobias.playpad.RegistryCollection#getAudioHandlerRegistry()
	 */
	@Override
	public AudioRegistry getAudioHandlers() {
		return audioHandlerRegistry;
	}

	/* (non-Javadoc)
	 * @see de.tobias.playpad.RegistryCollection#getDragModeRegistry()
	 */
	@Override
	public Registry<PadDragMode> getDragModes() {
		return dragModeRegistry;
	}

	/* (non-Javadoc)
	 * @see de.tobias.playpad.RegistryCollection#getLayoutRegistry()
	 */
	@Override
	public DefaultRegistry<DesignConnect> getDesigns() {
		return layoutRegistry;
	}

	/* (non-Javadoc)
	 * @see de.tobias.playpad.RegistryCollection#getMapperRegistry()
	 */
	@Override
	public Registry<MapperConnect> getMappers() {
		return mapperRegistry;
	}

	/* (non-Javadoc)
	 * @see de.tobias.playpad.RegistryCollection#getPadContentRegistry()
	 */
	@Override
	public Registry<PadContentConnect> getPadContents() {
		return padContentRegistry;
	}

	/* (non-Javadoc)
	 * @see de.tobias.playpad.RegistryCollection#getTriggerItemRegistry()
	 */
	@Override
	public Registry<TriggerItemConnect> getTriggerItems() {
		return triggerItemRegistry;
	}

}
