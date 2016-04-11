package de.tobias.playpad.viewcontroller.option.pad.trigger;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.trigger.CartTriggerItem;
import de.tobias.utils.ui.ContentViewController;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;


public class CartTriggerViewController extends ContentViewController {

	@FXML private ComboBox<PadStatus> statusComboBox;
	@FXML private CheckBox allCartsCheckbox;
	@FXML private TextField cartTextField;

	private CartTriggerItem item;

	public CartTriggerViewController(CartTriggerItem item) {
		super("cartTrigger", "de/tobias/playpad/assets/view/option/pad/trigger/", PlayPadMain.getUiResourceBundle());
		this.item = item;

		statusComboBox.setValue(item.getNewStatus());
		allCartsCheckbox.setSelected(item.isAllCarts());
		cartTextField.setText(item.getCartsString());
	}

	@Override
	public void init() {
		statusComboBox.getItems().addAll(PadStatus.PLAY, PadStatus.PAUSE, PadStatus.STOP);
		statusComboBox.valueProperty().addListener((a, b, c) -> item.setNewStatus(c));

		allCartsCheckbox.selectedProperty().addListener((a, b, c) ->
		{
			cartTextField.setDisable(c);
			item.setAllCarts(c);
		});
		cartTextField.textProperty().addListener((a, b, c) ->
		{
			if (c != null && !c.isEmpty())
				item.setCartsString(c);
		});
	}
}
