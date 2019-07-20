package de.tobias.playpad.viewcontroller.actions;

import de.thecodelabs.midi.action.Action;
import de.thecodelabs.utils.ui.NVC;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.actions.cart.CartAction;
import de.tobias.playpad.action.actions.cart.CartAction.CartActionMode;
import de.tobias.playpad.action.mapper.MapperViewController;
import de.tobias.playpad.viewcontroller.BaseMapperListViewController;
import de.tobias.playpad.viewcontroller.cell.EnumCell;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class CartActionViewController extends NVC {

	@FXML
	private ComboBox<CartActionMode> controlMode;
	@FXML
	private CheckBox autoColorCheckbox;

	@FXML
	private VBox rootContainer;
	private BaseMapperListViewController baseMapperListViewController;

	private Action action;

	public CartActionViewController() {
		load("view/actions", "CartAction", PlayPadMain.getUiResourceBundle());
	}

	@Override
	public void init() {
		controlMode.getItems().setAll(CartActionMode.values());
		controlMode.setCellFactory(list -> new EnumCell<>(Strings.CartAction_Mode_BaseName));
		controlMode.setButtonCell(new EnumCell<>(Strings.CartAction_Mode_BaseName));
		controlMode.valueProperty().addListener((observable, oldValue, newValue) ->
		{
			CartAction.setMode(action, newValue);
		});

		autoColorCheckbox.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			CartAction.setAutoFeedback(action, newValue);
			// Disable Feedback Controls bei Automatischen Feedback für VORHANDENE MAPPER
			if (baseMapperListViewController != null) {
				baseMapperListViewController.getControllers().forEach(this::toggleFeedbackVisibility);
			}
		});
		VBox.setVgrow(rootContainer, Priority.ALWAYS);

		baseMapperListViewController = BaseMapperListViewController.getInstance();
		baseMapperListViewController.addNewMapperListener((mapper, controller) ->
		{
			// Show/Hide Feedback settings, depending on the cart action settings
			toggleFeedbackVisibility(controller);
		});
	}

	public void setCartAction(Action action) {
		this.action = action;

		controlMode.setValue(CartAction.getMode(action));
		autoColorCheckbox.setSelected(CartAction.isAutoFeedback(action));
	}

	private void toggleFeedbackVisibility(MapperViewController controller) {
		if (CartAction.isAutoFeedback(action)) {
			controller.hideFeedback();
		} else {
			controller.showFeedback();
		}
	}
}
