package de.tobias.playpad.viewcontroller.dialog.project;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.ProjectReader;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.util.Localization;
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
		load("de/tobias/playpad/assets/dialog/project/", "loadDialog", PlayPadMain.getUiResourceBundle());

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.show();
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setWidth(400);
		stage.setHeight(100);

		stage.initModality(Modality.APPLICATION_MODAL);
		stage.initStyle(StageStyle.UNDECORATED);

		stage.setAlwaysOnTop(true);
		stage.centerOnScreen();
	}

	@Override
	public void startReadProject() {
		statusLabel.setText(Localization.getString(Strings.UI_Dialog_ProjectLoad_StartProject));
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
			statusLabel.setText(Localization.getString(Strings.UI_Dialog_ProjectLoad_StartPad, name));
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
}
