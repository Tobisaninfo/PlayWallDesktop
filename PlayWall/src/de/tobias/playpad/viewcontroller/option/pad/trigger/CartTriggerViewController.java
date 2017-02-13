package de.tobias.playpad.viewcontroller.option.pad.trigger;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.trigger.CartTriggerItem;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.controlsfx.control.textfield.TextFields;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class CartTriggerViewController extends NVC {

	@FXML private ComboBox<PadStatus> statusComboBox;
	@FXML private CheckBox allCartsCheckbox;
	@FXML private TextField cartTextField;
	@FXML private ListView<UUID> addedCarts;
	@FXML private Button addButton;

	private CartTriggerItem item;

	public CartTriggerViewController(CartTriggerItem item) {
		load("de/tobias/playpad/assets/view/option/pad/trigger/", "cartTrigger", PlayPadMain.getUiResourceBundle());
		this.item = item;

		statusComboBox.setValue(item.getNewStatus());
		allCartsCheckbox.setSelected(item.isAllCarts());
		addedCarts.getItems().setAll(item.getCarts());
	}

	@Override
	public void init() {
		statusComboBox.getItems().addAll(PadStatus.PLAY, PadStatus.PAUSE, PadStatus.STOP);
		statusComboBox.valueProperty().addListener((a, b, c) -> item.setNewStatus(c));

		allCartsCheckbox.selectedProperty().addListener((a, b, c) ->
		{
			item.setAllCarts(c);
		});

		// Auto Complete
		Project project = PlayPadMain.getProgramInstance().getCurrentProject();
		Set<String> names = project.getPads().stream().filter(p -> p.getStatus() != PadStatus.EMPTY).map(Pad::getName)
				.collect(Collectors.toSet());
		TextFields.bindAutoCompletion(cartTextField, names);

		addedCarts.setCellFactory(new Callback<ListView<UUID>, ListCell<UUID>>() {

			@Override
			public ListCell<UUID> call(ListView<UUID> param) {
				ListCell<UUID> cell = new ListCell<UUID>() {
					@Override
					protected void updateItem(UUID item, boolean empty) {
						super.updateItem(item, empty);
						if (!empty) {
							setGraphic(new Button("", new FontIcon(FontAwesomeType.TRASH)));
							setContentDisplay(ContentDisplay.RIGHT);
							Project project = PlayPadMain.getProgramInstance().getCurrentProject();
							setText(project.getPad(item).getName());
						}
					}
				};
				return cell;
			}
		});
	}

	@FXML
	private void addHandler(ActionEvent event) {
		Project project = PlayPadMain.getProgramInstance().getCurrentProject();
		for (Pad pad : project.getPads()) {
			if (pad.getStatus() != PadStatus.EMPTY) {
				if (pad.getName().equals(cartTextField.getText())) {
					item.getCarts().add(pad.getUuid());
					addedCarts.getItems().add(pad.getUuid());
				}
			}
		}
	}
}
