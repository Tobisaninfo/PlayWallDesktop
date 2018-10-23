package de.tobias.playpad.viewcontroller.option.pad.trigger;

import de.thecodelabs.utils.ui.NVC;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.trigger.CartTriggerItem;
import de.tobias.playpad.view.main.ProjectPreviewView;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

public class CartTriggerViewController extends NVC {

	@FXML
	private ComboBox<PadStatus> statusComboBox;
	@FXML
	private CheckBox allCartsCheckbox;

	private ProjectPreviewView projectPreviewView;

	private CartTriggerItem item;

	public CartTriggerViewController(CartTriggerItem item) {
		load("view/option/pad/trigger", "CartTrigger", PlayPadMain.getUiResourceBundle());
		this.item = item;

		Project project = PlayPadMain.getProgramInstance().getCurrentProject();
		final List<Pad> pads = item.getCarts().stream().map(project::getPad).collect(Collectors.toList());
		projectPreviewView = new ProjectPreviewView(project, pads);
		projectPreviewView.setPadding(new Insets(0, 0, 0, 164));
		projectPreviewView.selectedProperty().addListener((InvalidationListener) observable -> {
			item.getCarts().clear();
			for (Pad pad : projectPreviewView.getSelected()) {
				item.getCarts().add(pad.getUuid());
			}
		});
		VBox vBox = (VBox) getParent();
		vBox.getChildren().add(projectPreviewView);

		statusComboBox.setValue(item.getNewStatus());
		allCartsCheckbox.setSelected(item.isAllCarts());
	}

	@Override
	public void init() {
		statusComboBox.getItems().addAll(PadStatus.PLAY, PadStatus.PAUSE, PadStatus.STOP);
		statusComboBox.valueProperty().addListener((a, b, c) -> item.setNewStatus(c));

		allCartsCheckbox.selectedProperty().addListener((a, b, c) -> item.setAllCarts(c));
	}
}
