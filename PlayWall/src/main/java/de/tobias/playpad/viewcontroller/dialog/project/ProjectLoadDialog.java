package de.tobias.playpad.viewcontroller.dialog.project;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.ProjectReader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Created by tobias on 19.03.17.
 */
public class ProjectLoadDialog extends NVC implements ProjectReader.ProjectReaderListener {

	@FXML
	private Label statusLabel;
	@FXML
	private ProgressBar progressbar;

	public ProjectLoadDialog() {
		load("view/dialog/project", "LoadDialog", Localization.getBundle());
		applyViewControllerToStage();
	}

	@Override
	public void initStage(Stage stage) {
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

		stage.setWidth(400);
		stage.setHeight(70);

		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initStyle(StageStyle.UNDECORATED);

		PlayPadPlugin.styleable().applyStyle(stage);

		stage.setAlwaysOnTop(true);
		stage.centerOnScreen();
	}

	@Override
	public void startReadProject() {
		getStageContainer().ifPresent(NVCStage::show);
		statusLabel.setText(Localization.getString(Strings.UI_DIALOG_PROJECT_LOAD_START_PROJECT));
	}

	@Override
	public void finishReadProject() {
		statusLabel.setText("");
	}

	private int currentCount;
	private int itemCount;

	@Override
	public void readMedia(String name) {
		currentCount++;
		Platform.runLater(() -> {
			statusLabel.setText(Localization.getString(Strings.UI_DIALOG_PROJECT_LOAD_START_PAD, name));
			progressbar.setProgress(currentCount / (double) itemCount);
		});
	}

	@Override
	public void totalMedia(int size) {
		itemCount = size;
	}

	@Override
	public void finish() {
		Platform.runLater(() -> getStageContainer().ifPresent(NVCStage::close));
	}

	@Override
	public void abort() {
		Platform.runLater(() -> getStageContainer().ifPresent(NVCStage::close));
	}
}
