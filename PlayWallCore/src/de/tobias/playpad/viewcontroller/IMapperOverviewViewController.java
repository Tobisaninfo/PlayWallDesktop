package de.tobias.playpad.viewcontroller;

import java.util.List;

import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.action.mapper.MapperViewController;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;

public interface IMapperOverviewViewController {

	public static interface MapperAddListener {

		public void onAdd(Mapper mapper, MapperViewController controller);
	}

	public List<MapperViewController> getControllers();

	public void addMapperAddListener(MapperAddListener addListener);

	public void showAction(Action action, Pane parent);

	public void showAction(Action action, ScrollPane parent);

	public Parent getParent();

}