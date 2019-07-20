package de.tobias.playpad.view;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.mapping.Key;
import de.thecodelabs.midi.mapping.KeyRegistry;
import de.thecodelabs.midi.mapping.KeyType;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.mapper.MapperViewController;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.viewcontroller.BaseMapperListViewController;
import de.tobias.playpad.viewcontroller.mapper.KeyboardMapperViewController;
import de.tobias.playpad.viewcontroller.mapper.MidiMapperViewController;
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
import java.util.stream.Stream;

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

		final KeyType[] keys = KeyType.values();
		Stream.of(keys).forEach(item ->
		{
			// TODO Extract
			String name = "";
			switch (item) {
				case MIDI:
					Localization.getString(Strings.Mapper_Midi_Name);
					break;
				case KEYBOARD:
					Localization.getString(Strings.Mapper_Keyboard_Name);
					break;
			}

			Button button = new Button(name, new FontIcon(FontAwesomeType.PLUS_CIRCLE));
			button.setContentDisplay(ContentDisplay.TOP);
			button.setPrefWidth(150);

			button.setOnAction(e ->
			{
				// Adds a mapper to the action
				try {
					MapperViewController controller = onAddMapper(item, action);
					boolean result = controller.showInputMapperUI();

					// Delete Mapper wenn Eingabe abgebrochen wurde
					if (!result) {
						mappingView.getChildren().removeAll(controller.getParent().getParent());
						action.removeKey(controller.getKey());
					}
				} catch (NoSuchComponentException ex) {
					// TODO Error Handling
					Logger.error(e);
				}

			});
			addMappingBox.getChildren().add(button);
		});
	}

	private MapperViewController onAddMapper(KeyType type, Action action) throws NoSuchComponentException {
		try {
			KeyRegistry registry = KeyRegistry.getInstance();
			Key mapper = registry.getType(type).newInstance();
			action.addKey(mapper);
			return addMapperView(type, mapper);
		} catch (Exception e) {
			throw new NoSuchComponentException(e);
		}
	}

	private MapperViewController addMapperView(KeyType type, Key mapper) {
		MapperViewController controller = null;

		// TODO Extract
		switch (type) {
			case MIDI:
				controller = new MidiMapperViewController();
				break;
			case KEYBOARD:
				controller = new KeyboardMapperViewController();
				break;
		}

		if (controller != null) {
			Button deleteButton = new Button("", new FontIcon(FontAwesomeType.TRASH));

			HBox hbox = new HBox(controller.getParent(), deleteButton);
			hbox.setSpacing(14);

			mappingView.getChildren().addAll(hbox);

			deleteButton.setOnAction((e) ->
			{
				action.removeKey(mapper);
				mappingView.getChildren().removeAll(hbox);
			});
		}
		controllers.add(controller);
		MapperViewController finalController = controller;
		addListeners.forEach(i -> i.onAdd(mapper, finalController));
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

		List<Key> mapperSorted = action.getKeys();
		for (int i = 0; i < mapperSorted.size(); i++) {
			Key mapper = mapperSorted.get(i);
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
