package de.tobias.playpad;

import de.tobias.playpad.action.ActionConnect;
import de.tobias.playpad.action.mapper.MapperConnect;
import de.tobias.playpad.audio.AudioHandlerConnect;
import de.tobias.playpad.layout.LayoutConnect;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.pad.drag.PadDragMode;
import de.tobias.playpad.registry.ComponentRegistry;
import de.tobias.playpad.registry.DefaultComponentRegistry;
import de.tobias.playpad.registry.DefaultRegistry;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.tigger.TriggerItemConnect;

public class PlayPadRegistry {

	private static Registry<PadContentConnect> padContentRegistry;
	private static Registry<MapperConnect> mapperRegistry;
	private static DefaultRegistry<AudioHandlerConnect> audioHandlerRegistry;
	private static DefaultRegistry<LayoutConnect> layoutRegistry;
	private static Registry<TriggerItemConnect> triggerItemRegistry;
	private static Registry<ActionConnect> actionRegistry;
	private static Registry<PadDragMode> dragModeRegistry;

	static {
		actionRegistry = new ComponentRegistry<>("Action");
		audioHandlerRegistry = new DefaultComponentRegistry<>("AudioHandler");
		dragModeRegistry = new ComponentRegistry<>("DragMode");
		layoutRegistry = new DefaultComponentRegistry<>("Layout");
		mapperRegistry = new ComponentRegistry<>("Mapper");
		padContentRegistry = new ComponentRegistry<>("PadContent");
		triggerItemRegistry = new ComponentRegistry<>("Trigger");
	}

	public static Registry<ActionConnect> getActionRegistry() throws IllegalAccessException {
		return actionRegistry;
	}

	public static DefaultRegistry<AudioHandlerConnect> getAudioHandlerRegistry() throws IllegalAccessException {
		return audioHandlerRegistry;
	}

	public static Registry<PadDragMode> getDragModeRegistry() throws IllegalAccessException {
		return dragModeRegistry;
	}

	public static DefaultRegistry<LayoutConnect> getLayoutRegistry() throws IllegalAccessException {
		return layoutRegistry;
	}

	public static Registry<MapperConnect> getMapperRegistry() throws IllegalAccessException {
		return mapperRegistry;
	}

	public static Registry<PadContentConnect> getPadContentRegistry() throws IllegalAccessException {
		return padContentRegistry;
	}

	public static Registry<TriggerItemConnect> getTriggerItemRegistry() throws IllegalAccessException {
		return triggerItemRegistry;
	}

}
