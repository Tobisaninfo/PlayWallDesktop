package de.tobias.playpad.viewcontroller.option.pad.trigger;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.trigger.VolumeTriggerItem;
import de.tobias.playpad.view.main.ProjectPreviewView;
import javafx.beans.InvalidationListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.stream.Collectors;

public class VolumeTriggerViewController extends NVC {

	@FXML
	private Slider volumeSlider;
	@FXML
	private Label volumeLabel;

	@FXML
	private Slider durationSlider;
	@FXML
	private Label durationLabel;
	private ProjectPreviewView projectPreviewView;

	private VolumeTriggerItem item;

	public VolumeTriggerViewController(VolumeTriggerItem item) {
		load("view/option/pad/trigger", "VolumeTrigger", PlayPadMain.getUiResourceBundle());
		this.item = item;

		volumeSlider.setValue(item.getVolume() * 100.0);
		durationSlider.setValue(item.getDuration().toSeconds());

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

	}

	@Override
	public void init() {
		volumeSlider.valueProperty().addListener((a, b, c) ->
		{
			item.setVolume(c.doubleValue() / 100.0);
			volumeLabel.setText(Localization.getString(Strings.Standard_Time_Volume, Math.round(c.doubleValue())));
		});

		durationSlider.valueProperty().addListener((a, b, c) ->
		{
			item.setDuration(Duration.seconds(c.doubleValue()));

			double seconds = Math.round(item.getDuration().toSeconds() * 10.0) / 10.0;
			durationLabel.setText(Localization.getString(Strings.Standard_Time_Seconds, seconds));
		});

	}
}
