package de.tobias.playpad.viewcontroller.actions;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.midi.Mapping;
import de.thecodelabs.midi.action.Action;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.action.actions.CartAction;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import org.controlsfx.control.SegmentedButton;

import java.util.List;

/**
 * Diese View ist die Basis für die Einstellunge für eine CartAction. Dabei enthällt diese View ein Grid aus Buttons (Carts), eine
 * seitenauswahl (Carts ändern sich) und eine Scrollview für die Einstellungen. Die Einstellungen werden von der Class
 * CartActionViewController hier eingebettet. Dabei wird nicht jedes mal eine neue Instance erstellt, sondern die in CartAction vorhandene
 * Instance verwendet. Das geht, solange die View nur einmal verwendet wird.
 *
 * @author tobias
 */
public class CartActionTypeViewController extends NVC {

	@FXML
	private VBox buttonVbox;
	@FXML
	private GridPane gridPane;
	@FXML
	private VBox cartActionContainer;

	private final AbstractActionViewController actionViewController;
	private final String actionType;

	private final Mapping mapping;
	private final IMappingTabViewController parentController;


	public CartActionTypeViewController(Mapping mapping, IMappingTabViewController parentController, String actionType) {
		this(mapping, parentController, actionType,null);
	}

	public CartActionTypeViewController(Mapping mapping, IMappingTabViewController parentController, String actionType, AbstractActionViewController actionViewController) {
		load("view/actions", "CartActions", Localization.getBundle());
		this.mapping = mapping;
		this.parentController = parentController;

		this.actionType = actionType;
		this.actionViewController = actionViewController;

		Project currentProject = PlayPadMain.getProgramInstance().getCurrentProject();
		ProjectSettings settings = currentProject.getSettings();

		showCartButtons(settings);
		VBox.setVgrow(gridPane, Priority.ALWAYS);
	}

	@Override
	public void init() {
		buttonVbox.minHeightProperty().bind(buttonVbox.heightProperty());
	}

	@SuppressWarnings("Duplicates")
	private void showCartButtons(ProjectSettings settings) {
		gridPane.getChildren().clear();

		gridPane.getColumnConstraints().clear();
		double xPercentage = 1.0 / settings.getColumns();
		for (int i = 0; i < settings.getColumns(); i++) {
			ColumnConstraints c = new ColumnConstraints();
			c.setPercentWidth(xPercentage * 100);
			gridPane.getColumnConstraints().add(c);
		}

		gridPane.getRowConstraints().clear();
		double yPercentage = 1.0 / settings.getRows();
		for (int i = 0; i < settings.getRows(); i++) {
			RowConstraints c = new RowConstraints();
			c.setPercentHeight(yPercentage * 100);
			gridPane.getRowConstraints().add(c);
		}

		ToggleGroup cartsToggle = new ToggleGroup();

		int index = 0;

		for (int y = 0; y < settings.getRows(); y++) {
			for (int x = 0; x < settings.getColumns(); x++) {
				ToggleButton button = new ToggleButton(String.valueOf(index++ + 1));
				button.setMaxWidth(Double.MAX_VALUE);
				button.setUserData(new int[]{x, y});
				button.getStyleClass().add(SegmentedButton.STYLE_CLASS_DARK);

				// Show the right cart settings
				button.selectedProperty().addListener((observable, oldValue, newValue) -> toggleButtonHandler(button, newValue));
				gridPane.add(button, x, y);
				cartsToggle.getToggles().add(button);
			}
		}
	}

	private void toggleButtonHandler(ToggleButton button, Boolean newValue) {
		if (newValue) {
			int[] data = (int[]) button.getUserData();
			int currentX = data[0];
			int currentY = data[1];

			try {
				List<Action> cartActions = mapping.getActionsForType(actionType);
				for (Action action : cartActions) {
					if (CartAction.getX(action) == currentX && CartAction.getY(action) == currentY) {
						if (actionViewController != null) {
							cartActionContainer.getChildren().setAll(actionViewController.getParent());
							cartActionContainer.setVisible(true);
							actionViewController.setCartAction(action);
						}
						parentController.showMapperFor(action);
					}
				}
			} catch (NoSuchComponentException e) {
				Logger.error(e);
			}
		} else {
			cartActionContainer.setVisible(false);
			parentController.showMapperFor(null);
		}
	}
}
