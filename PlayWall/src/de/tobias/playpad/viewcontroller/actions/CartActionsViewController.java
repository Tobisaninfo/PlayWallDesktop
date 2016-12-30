package de.tobias.playpad.viewcontroller.actions;

import java.util.List;

import org.controlsfx.control.SegmentedButton;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.action.Mapping;
import de.tobias.playpad.action.cartaction.CartAction;
import de.tobias.playpad.action.connect.CartActionConnect;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.viewcontroller.IMappingTabViewController;
import de.tobias.utils.ui.ContentViewController;
import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

/**
 * Diese View ist die Basis für die Einstellunge für eine CartAction. Dabei enthällt diese View ein Grid aus Buttons (Carts), eine
 * seitenauswahl (Carts ändern sich) und eine Scrollview für die Einstellungen. Die Einstellungen werden von der Class
 * CartActionViewController hier eingebettet. Dabei wird nicht jedes mal eine neue Instance erstellt, sondern die in CartAction vorhandene
 * Instance verwendet. Das geht, solange die View nur einmal verwendet wird.
 * 
 * @author tobias
 *
 */
public class CartActionsViewController extends ContentViewController {

	@FXML private VBox buttonVbox;

	private ToggleGroup cartsToggle;
	@FXML private GridPane gridPane;

	@FXML private VBox cartActionContainer;

	private Mapping mapping;
	private IMappingTabViewController parentController;

	public CartActionsViewController(Mapping mapping, IMappingTabViewController parentController) {
		super("cartActions", "de/tobias/playpad/assets/view/actions/", PlayPadMain.getUiResourceBundle());
		this.mapping = mapping;
		this.parentController = parentController;

		Project currentProject = PlayPadMain.getProgramInstance().getCurrentProject();
		ProjectSettings settings = currentProject.getSettings();

		showCartButtons(settings, 0);
		VBox.setVgrow(gridPane, Priority.ALWAYS);
	}

	@Override
	public void init() {
		buttonVbox.minHeightProperty().bind(buttonVbox.heightProperty());
	}

	private void showCartButtons(ProjectSettings settings, int page) {
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

		cartsToggle = new ToggleGroup();

		int index = 0;

		for (int y = 0; y < settings.getRows(); y++) {
			for (int x = 0; x < settings.getColumns(); x++) {
				ToggleButton button = new ToggleButton(String.valueOf(index++ + 1));
				button.setMaxWidth(Double.MAX_VALUE);
				button.setUserData(new int[] { x, y });
				button.getStyleClass().add(SegmentedButton.STYLE_CLASS_DARK);

				// Show the right cart settings
				button.selectedProperty().addListener((a, b, c) ->
				{
					if (c) {
						int[] data = (int[]) button.getUserData();
						int currentX = data[0];
						int currentY = data[1];

						List<CartAction> cartActions = mapping.getActions(CartActionConnect.TYPE);
						for (CartAction action : cartActions) {
							if (action.getX() == currentX && action.getY() == currentY) {
								ContentViewController actionViewController = action.getSettingsViewController();
								cartActionContainer.getChildren().setAll(actionViewController.getParent());
								cartActionContainer.setVisible(true);
								parentController.showMapperFor(action);
							}
						}
					} else {
						cartActionContainer.setVisible(false);
						parentController.showMapperFor(null);
					}
				});
				gridPane.add(button, x, y);
				cartsToggle.getToggles().add(button);
			}
		}
	}
}
