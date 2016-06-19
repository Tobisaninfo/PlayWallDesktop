package de.tobias.playpad.viewcontroller.option;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.Strings;
import de.tobias.playpad.layout.GlobalLayout;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.viewcontroller.SettingsTabViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.NumberUtils;
import de.tobias.utils.util.Worker;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class GeneralTabViewController extends SettingsTabViewController {

	private static final String DIGIT_POSITIV = "^[1-9]\\d*$";

	private Screen mainWindowScreen;
	private ViewController parentController; // Für Benachrichtungen

	@FXML private TextField pageCountTextField;
	@FXML private TextField columnTextField;
	@FXML private TextField rowTextField;

	@FXML private CheckBox liveModeCheckBox;

	@FXML private TextField cacheTextField;
	@FXML private Label cacheSizeLabel;

	@FXML private RadioButton pageEnable;
	@FXML private RadioButton pageDisable;
	@FXML private ToggleGroup pageGroup;
	@FXML private RadioButton dragEnable;
	@FXML private RadioButton dragDisable;
	@FXML private ToggleGroup dragGroup;
	@FXML private RadioButton fileEnable;
	@FXML private RadioButton fileDisable;
	@FXML private ToggleGroup fileGroup;
	@FXML private RadioButton settingsEnable;
	@FXML private RadioButton settingsDisable;
	@FXML private ToggleGroup settingsGroup;

	private boolean changeSettings;

	public GeneralTabViewController(Screen screen, ViewController parentController, boolean activePlayer) {
		super("generalTab", "de/tobias/playpad/assets/view/option/", PlayPadMain.getUiResourceBundle());
		this.mainWindowScreen = screen;
		this.parentController = parentController;

		if (activePlayer) {
			rowTextField.setDisable(true);
			columnTextField.setDisable(true);
			pageCountTextField.setDisable(true);
		}
		calcCacheSize();
	}

	@Override
	public void init() {
		pageCountTextField.textProperty().addListener((a, b, c) ->
		{
			if (c.matches(DIGIT_POSITIV) && !c.isEmpty()) {
				int number = Integer.valueOf(c);
				if (number > ProfileSettings.MAX_PAGES) {
					pageCountTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true); // Zahl zu groß
				} else {
					pageCountTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, false); // Zahl ok
				}
			} else {
				pageCountTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true); // Negativ oder leer
			}
		});

		columnTextField.textProperty().addListener((a, b, c) ->
		{
			if (c.matches(DIGIT_POSITIV) && !c.isEmpty()) {
				if (screenValid()) {
					columnTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, false); // Zahl ok
				} else {
					columnTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true); // Zahl zu groß
				}
			} else {
				columnTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true); // Negativ oder leer
			}
		});

		rowTextField.textProperty().addListener((a, b, c) ->
		{
			if (c.matches(DIGIT_POSITIV) && !c.isEmpty()) {
				if (screenValid()) {
					rowTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, false); // Zahl ok
				} else {
					rowTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true); // Zahl zu groß
				}
			} else {
				rowTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true); // Negativ oder leer
			}
		});

		pageGroup = new ToggleGroup();
		pageGroup.getToggles().addAll(pageEnable, pageDisable);
		dragGroup = new ToggleGroup();
		dragGroup.getToggles().addAll(dragEnable, dragDisable);
		fileGroup = new ToggleGroup();
		fileGroup.getToggles().addAll(fileEnable, fileDisable);
		settingsGroup = new ToggleGroup();
		settingsGroup.getToggles().addAll(settingsEnable, settingsDisable);

		liveModeCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			disableLiveSettings(c);
		});
	}

	private void disableLiveSettings(Boolean enableLiveSettings) {
		pageEnable.setDisable(!enableLiveSettings);
		pageDisable.setDisable(!enableLiveSettings);
		dragEnable.setDisable(!enableLiveSettings);
		dragDisable.setDisable(!enableLiveSettings);
		fileEnable.setDisable(!enableLiveSettings);
		fileDisable.setDisable(!enableLiveSettings);
		settingsEnable.setDisable(!enableLiveSettings);
		settingsDisable.setDisable(!enableLiveSettings);
	}

	@FXML
	private void cacheChooseHandler(ActionEvent event) {
		DirectoryChooser chooser = new DirectoryChooser();
		File folder = chooser.showDialog(getStage());
		if (folder != null) {
			Path folderPath = folder.toPath();
			Profile.currentProfile().getProfileSettings().setCachePath(folderPath);
			cacheTextField.setText(folderPath.toString());
		}
	}

	@FXML
	private void cacheResetButtonHandler(ActionEvent event) {
		try {
			int deleteFiles = 0;
			for (Path path : Files.newDirectoryStream(ApplicationUtils.getApplication().getPath(PathType.CACHE))) {
				if (Files.isRegularFile(path)) {
					try {
						Files.delete(path);
						deleteFiles++;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			parentController.showInfoMessage(Localization.getString(Strings.Info_Settings_CacheDelete, deleteFiles),
					PlayPadMain.stageIcon.get());

			calcCacheSize();
		} catch (IOException e) {
			e.printStackTrace();
			showErrorMessage(Localization.getString(Strings.Error_Settings_CacheClear, e.getLocalizedMessage()));
		}
	}

	@FXML
	private void resetDialogs(ActionEvent event) {
		parentController.showInfoMessage(Localization.getString(Strings.Info_Settings_ResetWarning));
	}

	private void calcCacheSize() {
		try {
			double size = 0;
			Path path = Profile.currentProfile().getProfileSettings().getCachePath();
			if (Files.notExists(path))
				Files.createDirectories(path);

			for (Path item : Files.newDirectoryStream(Profile.currentProfile().getProfileSettings().getCachePath())) {
				size += Files.size(item);
			}
			cacheSizeLabel.setText(Localization.getString(Strings.UI_Window_Settings_Gen_CacheSize, NumberUtils.numberToString(size)));
		} catch (IOException e) {
			e.printStackTrace();
			parentController.showErrorMessage(Localization.getString(Strings.Error_Settings_CacheSize, e.getMessage()), PlayPadMain.stageIcon);
		}
	}

	private boolean screenValid() {
		double width = mainWindowScreen.getVisualBounds().getMaxX() - mainWindowScreen.getVisualBounds().getMinX();
		double height = mainWindowScreen.getVisualBounds().getMaxY() - mainWindowScreen.getVisualBounds().getMinY();

		GlobalLayout layout = Profile.currentProfile().currentLayout();

		try {
			double neededWidth = layout.getMinWidth(Integer.valueOf(columnTextField.getText()));
			double neededHeight = layout.getMinHeight(Integer.valueOf(rowTextField.getText())) + 100;

			if (neededHeight <= height && neededWidth <= width)
				return true;
		} catch (NumberFormatException e) {}
		return false;
	}

	@Override
	public void loadSettings(Profile profile) {
		ProfileSettings profileSettings = profile.getProfileSettings();

		pageCountTextField.setText(String.valueOf(profileSettings.getPageCount()));
		columnTextField.setText(String.valueOf(profileSettings.getColumns()));
		rowTextField.setText(String.valueOf(profileSettings.getRows()));

		liveModeCheckBox.setSelected(profileSettings.isLiveMode());
		cacheTextField.setText(profileSettings.getCachePath().toString());

		if (screenValid()) {
			columnTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, false);
			rowTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, false);
		} else {
			columnTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true);
			rowTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true);
		}

		if (profileSettings.isLiveModePage() == true)
			pageEnable.setSelected(true);
		else
			pageDisable.setSelected(true);

		if (profileSettings.isLiveModeDrag() == true)
			dragEnable.setSelected(true);
		else
			dragDisable.setSelected(true);

		if (profileSettings.isLiveModeFile() == true)
			fileEnable.setSelected(true);
		else
			fileDisable.setSelected(true);

		if (profileSettings.isLiveModeSettings() == true)
			settingsEnable.setSelected(true);
		else
			settingsDisable.setSelected(true);

		disableLiveSettings(profileSettings.isLiveMode());
	}

	@Override
	public void saveSettings(Profile profile) {
		ProfileSettings profileSettings = profile.getProfileSettings();

		int columns = Integer.valueOf(columnTextField.getText());
		int rows = Integer.valueOf(rowTextField.getText());
		int pageCount = Integer.valueOf(pageCountTextField.getText());

		if (profileSettings.getColumns() != columns || profileSettings.getRows() != rows || profileSettings.getPageCount() != pageCount)
			changeSettings = true;
		else
			changeSettings = false;

		// Copy Settings
		profileSettings.setColumns(columns);
		profileSettings.setRows(rows);
		profileSettings.setPageCount(pageCount);

		profileSettings.setLiveMode(liveModeCheckBox.isSelected());
		profileSettings.setCachePath(Paths.get(cacheTextField.getText()));

		profileSettings.setLiveModePage(pageEnable.isSelected());
		profileSettings.setLiveModeDrag(dragEnable.isSelected());
		profileSettings.setLiveModeFile(fileEnable.isSelected());
		profileSettings.setLiveModeSettings(settingsEnable.isSelected());
	}

	@Override
	public boolean needReload() {
		return changeSettings;
	}

	@Override
	public void reload(Profile profile, Project project, IMainViewController controller) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setContentText(Localization.getString(Strings.UI_Window_Settings_Gen_Wait));

		alert.getButtonTypes().clear();
		alert.initOwner(controller.getStage());
		alert.initModality(Modality.WINDOW_MODAL);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		alert.show();

		Worker.runLater(() ->
		{
			Platform.runLater(() ->
			{
				controller.getToolbarController().createPageButtons();
				controller.createPadViews();
				controller.showPage(controller.getPage());
				stage.close();
			});
		});

	}

	@Override
	public boolean validSettings() {
		if (screenValid()) {
			return true;
		} else {
			double width = mainWindowScreen.getVisualBounds().getMaxX() - mainWindowScreen.getVisualBounds().getMinX();
			double height = mainWindowScreen.getVisualBounds().getMaxY() - mainWindowScreen.getVisualBounds().getMinY();

			GlobalLayout globalLayout = Profile.currentProfile().currentLayout();

			int maxCartsX = (int) (width / globalLayout.getPadWidth());
			int maxCartsY = (int) ((height - 100) / globalLayout.getPadHeight());
			parentController.showErrorMessage(Localization.getString(Strings.Error_Profile_SmallScreen, maxCartsX, maxCartsY),
					PlayPadMain.stageIcon.orElse(null));
			return false;
		}
	}

	@Override
	public String name() {
		return Localization.getString(Strings.UI_Window_Settings_Gen_Title);
	}
}
