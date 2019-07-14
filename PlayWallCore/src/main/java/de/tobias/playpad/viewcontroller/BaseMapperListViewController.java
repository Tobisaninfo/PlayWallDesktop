package de.tobias.playpad.viewcontroller;

import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.mapping.Key;
import de.tobias.playpad.action.mapper.MapperViewController;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;

import java.util.List;

public abstract class BaseMapperListViewController {

	private static BaseMapperListViewController instance;

	public static BaseMapperListViewController getInstance() {
		return instance;
	}

	public static void setInstance(BaseMapperListViewController instance) {
		BaseMapperListViewController.instance = instance;
	}

	public interface MapperAddListener {

		void onAdd(Key mapper, MapperViewController controller);
	}

	public abstract List<MapperViewController> getControllers();

	public abstract void addNewMapperListener(MapperAddListener addListener);

	public abstract void showAction(Action action, Pane parent);

	public abstract void showAction(Action action, ScrollPane parent);

	public abstract Parent getParent();

}