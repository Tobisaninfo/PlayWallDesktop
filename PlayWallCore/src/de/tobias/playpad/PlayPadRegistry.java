package de.tobias.playpad;

import de.tobias.playpad.action.ActionConnect;
import de.tobias.playpad.action.mapper.MapperConnect;
import de.tobias.playpad.audio.AudioHandlerConnect;
import de.tobias.playpad.layout.LayoutConnect;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.pad.drag.PadDragMode;
import de.tobias.playpad.registry.ComponentRegistry;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.tigger.TriggerItemConnect;

public class PlayPadRegistry {

	private static Registry<PadContentConnect> padContentRegistry;
	private static Registry<MapperConnect> mapperRegistry;
	private static Registry<AudioHandlerConnect> audioHandlerRegistry;
	private static Registry<LayoutConnect> layoutRegistry;
	private static Registry<TriggerItemConnect> triggerItemRegistry;
	private static Registry<ActionConnect> actionRegistry;
	private static Registry<PadDragMode> dragModeRegistry;

	static {
		padContentRegistry = new ComponentRegistry<>();
		mapperRegistry = new ComponentRegistry<>();
		audioHandlerRegistry = new ComponentRegistry<>();
		layoutRegistry = new ComponentRegistry<>();
		triggerItemRegistry = new ComponentRegistry<>();
		actionRegistry = new ComponentRegistry<>();
		dragModeRegistry = new ComponentRegistry<>();
	}

	public static Registry<PadContentConnect> getPadContentRegistry() {
		return padContentRegistry;
	}

	public static Registry<MapperConnect> getMapperRegistry() {
		return mapperRegistry;
	}

	public static Registry<AudioHandlerConnect> getAudioHandlerRegistry() {
		return audioHandlerRegistry;
	}

	public static Registry<LayoutConnect> getLayoutRegistry() {
		return layoutRegistry;
	}

	public static Registry<TriggerItemConnect> getTriggerItemRegistry() {
		return triggerItemRegistry;
	}

	public static Registry<ActionConnect> getActionRegistry() {
		return actionRegistry;
	}

	public static Registry<PadDragMode> getDragModeRegistry() {
		return dragModeRegistry;
	}
}
