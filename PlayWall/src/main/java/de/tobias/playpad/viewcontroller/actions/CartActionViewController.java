package de.tobias.playpad.viewcontroller.actions;

import de.thecodelabs.midi.action.Action;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.actions.CartAction;
import de.tobias.playpad.action.actions.CartAction.CartActionMode;
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

	private Action action;

	public CartActionViewController() {
		load("view/actions", "CartAction", Localization.getBundle());
	}

	@Override
	public void init() {
		controlMode.getItems().setAll(CartActionMode.values());
		controlMode.setCellFactory(list -> new EnumCell<>(Strings.CartAction_Mode_BaseName));
		controlMode.setButtonCell(new EnumCell<>(Strings.CartAction_Mode_BaseName));
		controlMode.valueProperty().addListener((observable, oldValue, newValue) -> CartAction.setMode(action, newValue));

		autoColorCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> CartAction.setAutoFeedback(action, newValue));

		VBox.setVgrow(rootContainer, Priority.ALWAYS);
	}

	public void setCartAction(Action action) {
		this.action = action;

		controlMode.setValue(CartAction.getMode(action));
		autoColorCheckbox.setSelected(CartAction.isAutoFeedback(action));
	}
}
