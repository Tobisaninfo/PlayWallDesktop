package de.tobias.playpad.view;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.mapper.Mapper;
import de.tobias.playpad.action.mapper.MapperFactory;
import de.tobias.playpad.action.mapper.MapperViewController;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.viewcontroller.BaseMapperListViewController;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Diese Klasse zeigt die Mapper zu einer Action an
public class MapperListViewControllerImpl extends BaseMapperListViewController {

	private VBox root;
	private VBox mappingView;
	private HBox addMappingBox;

	private Action action;
	private List<MapperViewController> controllers;

	private List<MapperAddListener> addListeners;

	private Pane parent;
	private ScrollPane scrollPane;

	public MapperListViewControllerImpl() {
		controllers = new ArrayList<>();
		addListeners = new ArrayList<>();

		init();
	}

	public void init() {
		root = new VBox(14);

		mappingView = new VBox();
		mappingView.setSpacing(14);

		addMappingBox = new HBox(14);

		Label headline = new Label(PlayPadMain.getUiResourceBundle().getString("action.mapper.headline"));
		headline.setUnderline(true);
		root.getChildren().addAll(headline, mappingView, addMappingBox);

		Registry<MapperFactory> registry = PlayPadPlugin.getRegistryCollection().getMappers();
		Set<String> types = registry.getTypes();
		types.stream().sorted().forEach(item ->
		{
			String name = item;
			try {
				MapperFactory connect = registry.getFactory(item);
				name = connect.toString();
			} catch (NoSuchComponentException e) {
				// TODO Error Handling
				e.printStackTrace();
			}
			Button button = new Button(name, new FontIcon(FontAwesomeType.PLUS_CIRCLE));
			button.setContentDisplay(ContentDisplay.TOP);
			button.setPrefWidth(150);

			button.setOnAction(e ->
			{
				// Adds a mapper to the action
				try {
					MapperViewController controller = onAddMapper(item);
					boolean result = controller.showInputMapperUI();

					// Delete Mapper wenn Eingabe abgebrochen wurde
					if (!result) {
						mappingView.getChildren().removeAll(controller.getParent().getParent());
						action.removeMapper(controller.getMapper());
					}
				} catch (NoSuchComponentException ex) {
					// TODO Error Handling
					ex.printStackTrace();
				}

			});
			addMappingBox.getChildren().add(button);
		});
	}

	private MapperViewController onAddMapper(String type) throws NoSuchComponentException {
		Registry<MapperFactory> registry = PlayPadPlugin.getRegistryCollection().getMappers();

		Mapper mapper = registry.getFactory(type).createNewMapper();
		action.addMapper(mapper);
		return addMapperView(type, mapper);
	}

	private MapperViewController addMapperView(String type, Mapper mapper) {
		MapperViewController controller = (MapperViewController) mapper.getSettingsViewController();
		if (controller != null) {
			Button deleteButton = new Button("", new FontIcon(FontAwesomeType.TRASH));

			HBox hbox = new HBox(controller.getParent(), deleteButton);
			hbox.setSpacing(14);

			mappingView.getChildren().addAll(hbox);

			deleteButton.setOnAction((e) ->
			{
				action.removeMapper(mapper);
				mappingView.getChildren().removeAll(hbox);
			});
		}
		controllers.add(controller);
		addListeners.forEach(i -> i.onAdd(mapper, controller));
		return controller;
	}

	@Override
	public List<MapperViewController> getControllers() {
		return controllers;
	}

	@Override
	public Parent getParent() {
		return root;
	}

	@Override
	public void addNewMapperListener(MapperAddListener addListener) {
		addListeners.add(addListener);
	}

	private void createSubViews(Action action) {
		controllers.clear();
		mappingView.getChildren().clear();

		List<Mapper> mapperSorted = action.getMapperSorted();
		for (int i = 0; i < mapperSorted.size(); i++) {
			Mapper mapper = mapperSorted.get(i);
			addMapperView(mapper.getType(), mapper);
		}
	}

	@Override
	public void showAction(Action action, Pane parent) {
		this.action = action;

		createSubViews(action);

		if (this.parent != null) {
			this.parent.getChildren().remove(getParent()); // Rmeove from old Parent
		}
		if (this.scrollPane != null) {
			this.scrollPane.setContent(null);
		}

		this.parent = parent;
		this.parent.getChildren().add(getParent()); // Add to new Parent
	}

	@Override
	public void showAction(Action action, ScrollPane parent) {
		this.action = action;

		createSubViews(action);

		if (this.parent != null) {
			this.parent.getChildren().remove(getParent()); // Rmeove from old Parent
		}
		if (this.scrollPane != null) {
			this.scrollPane.setContent(null);
		}

		this.scrollPane = parent;
		this.scrollPane.setContent(getParent()); // Add to new Parent
	}
}
