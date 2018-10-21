package de.tobias.playpad.viewcontroller.dialog;

import com.itextpdf.text.DocumentException;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.design.modern.ModernGlobalDesign;
import de.tobias.playpad.log.LogSeason;
import de.tobias.playpad.log.LogSeasons;
import de.tobias.playpad.log.PlayoutLogPdfExport;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;
import de.tobias.utils.ui.NVC;
import de.tobias.utils.ui.NVCStage;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class PlayoutLogViewController extends NVC {

	@FXML
	private TextField nameTextField;
	@FXML
	private ListView<LogSeason> logList;
	@FXML
	private Button startButton;
	@FXML
	private Button exportButton;
	@FXML
	private Button deleteButton;
	@FXML
	private Button finishButton;

	private FontIcon logIcon;

	// TODO l10n
	public PlayoutLogViewController(Window owner) {
		load("view/dialog", "PlayoutLogDialog", PlayPadMain.getUiResourceBundle());

		NVCStage stage = applyViewControllerToStage();
		stage.initOwner(owner);
		stage.initModality(Modality.WINDOW_MODAL);
		addCloseKeyShortcut(stage::close);
	}

	@Override
	public void init() {
		logList.getItems().setAll(LogSeasons.getAllLogSeasonsLazy());

		// LogIcon
		logIcon = new FontIcon(FontAwesomeType.LIST);

		if (LogSeasons.getInstance() != null) { // Running
			startButton.setText("Stop"); // TODO l10n
			nameTextField.setDisable(true);
			nameTextField.setText(LogSeasons.getInstance().getName());
		} else {
			startButton.setText("Start"); // TODO l10n
			nameTextField.setDisable(false);
		}
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setTitle(Localization.getString(Strings.UI_Dialog_PlayoutLog_Title));
		stage.setMinWidth(375);
		stage.setMinHeight(400);

		stage.initModality(Modality.WINDOW_MODAL);

		ModernGlobalDesign design = Profile.currentProfile().getProfileSettings().getDesign();
		PlayPadPlugin.getModernDesignHandler().getModernGlobalDesignHandler().applyCss(design, stage);
	}

	@FXML
	private void startButtonHandler(ActionEvent event) {
		final MenuToolbarViewController controller = PlayPadPlugin.getImplementation().getMainViewController().getMenuToolbarController();

		if (LogSeasons.getInstance() == null) { // Start
			if (nameTextField.getText().isEmpty()) {
				return;
			}

			final Project currentProject = PlayPadPlugin.getImplementation().getCurrentProject();
			final ProjectSettings settings = currentProject.getSettings();

			LogSeason logSeason = LogSeasons.createLogSeason(nameTextField.getText(), settings.getColumns(), settings.getRows());
			logSeason.createProjectSnapshot(currentProject);

			controller.addToolbarItem(logIcon);
			startButton.setText("Stop"); // TODO l10n
			nameTextField.setDisable(false);
		} else { // Stop
			LogSeasons.stop();
			controller.removeToolbarItem(logIcon);
			startButton.setText("Start"); // TODO l10n
			nameTextField.setDisable(false);
		}
	}

	@FXML
	private void exportButtonHandler(ActionEvent event) {
		getSelectedLogSeason().ifPresent(season -> { // Lazy Season
			FileChooser fileChooser = new FileChooser();
			FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("PDF", "*.pdf");
			fileChooser.getExtensionFilters().add(extensionFilter);

			File file = fileChooser.showSaveDialog(getContainingWindow());
			if (file != null) {
				try {
					PlayoutLogPdfExport.createPdfFile(file.toPath(), LogSeasons.getLogSeason(season.getId()));
				} catch (IOException | DocumentException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@FXML
	private void deleteButtonHandler(ActionEvent event) {

	}

	@FXML
	private void finishButtonHandler(ActionEvent event) {
		getStageContainer().ifPresent(NVCStage::close);
	}

	private Optional<LogSeason> getSelectedLogSeason() {
		return Optional.ofNullable(logList.getSelectionModel().getSelectedItem());
	}
}
