package de.tobias.playpad.plugin.api.trigger;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.plugin.api.WebApiPlugin$;
import de.tobias.playpad.plugin.api.cell.WebApiRemoteCell;
import de.tobias.playpad.plugin.api.settings.WebApiRemoteSettings;
import de.tobias.playpad.project.api.IPad;
import de.tobias.playpad.view.main.ProjectPreviewView;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

public class RemoteTriggerItemSettingsController extends NVC {
	@FXML
	private ComboBox<PadStatus> statusComboBox;
	@FXML
	private ComboBox<WebApiRemoteSettings> remoteComboBox;

	private ProjectPreviewView projectPreviewView;

	private final RemoteTriggerItem item;

	public RemoteTriggerItemSettingsController(RemoteTriggerItem item) {
		load("plugin/webapi/view", "RemoteTrigger", Localization.getBundle());
		this.item = item;

		showProjectPreview();

		statusComboBox.setValue(item.getNewStatus());
		remoteComboBox.setValue(WebApiPlugin$.MODULE$.connections().keySet().stream()
				.filter(server -> server.getId().equals(item.getServerId()))
				.findFirst()
				.orElse(null)
		);
	}

	private void showProjectPreview() {
		WebApiPlugin$.MODULE$.getConnection(item.getServerId()).ifPresent(client -> {
			client.getCurrentProject(project -> {
				Platform.runLater(() -> {
					// Remove old node from tree
					if (projectPreviewView != null) {
						((VBox) getParent()).getChildren().remove(projectPreviewView);
						projectPreviewView = null;
					}

					final List<? extends IPad> pads = item.getCarts().stream().map(project::getPad).collect(Collectors.toList());
					projectPreviewView = new ProjectPreviewView(project, pads, 0);
					projectPreviewView.setPadding(new Insets(0, 0, 0, 164));
					projectPreviewView.selectedProperty().addListener((InvalidationListener) observable -> {
						item.getCarts().clear();
						for (IPad pad : projectPreviewView.getSelected()) {
							item.getCarts().add(pad.getUuid());
						}
					});
					VBox vBox = (VBox) getParent();
					vBox.getChildren().add(projectPreviewView);
				});
			});
		});
	}

	@Override
	public void init() {
		statusComboBox.getItems().setAll(PadStatus.PLAY, PadStatus.PAUSE, PadStatus.STOP);
		statusComboBox.valueProperty().addListener((a, b, c) -> item.setNewStatus(c));

		remoteComboBox.getItems().setAll(WebApiPlugin$.MODULE$.connections().keySet());
		remoteComboBox.setCellFactory(list -> new WebApiRemoteCell());
		remoteComboBox.setButtonCell(new WebApiRemoteCell());
		remoteComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			item.setServerId(newValue.getId());
			showProjectPreview();
		});
	}
}
