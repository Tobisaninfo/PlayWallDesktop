package de.tobias.playpad.viewcontroller;

import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.action.mapper.MapperViewController;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;

import java.util.List;

public abstract class BaseMapperOverviewViewController {

	private static BaseMapperOverviewViewController instance;

	public static BaseMapperOverviewViewController getInstance() {
		return instance;
	}

	public static void setInstance(BaseMapperOverviewViewController instance) {
		BaseMapperOverviewViewController.instance = instance;
	}

	public interface MapperAddListener {

		void onAdd(Mapper mapper, MapperViewController controller);
	}

	public abstract List<MapperViewController> getControllers();

	public abstract void addMapperAddListener(MapperAddListener addListener);

	public abstract void showAction(Action action, Pane parent);

	public abstract void showAction(Action action, ScrollPane parent);

	public abstract Parent getParent();

}