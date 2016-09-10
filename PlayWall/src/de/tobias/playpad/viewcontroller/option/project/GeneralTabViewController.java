package de.tobias.playpad.viewcontroller.option.project;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.Strings;
import de.tobias.playpad.design.GlobalDesign;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.IProjectReloadTask;
import de.tobias.playpad.viewcontroller.option.ProjectSettingsTabViewController;
import de.tobias.utils.ui.Alertable;
import de.tobias.utils.util.Localization;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Screen;

public class GeneralTabViewController extends ProjectSettingsTabViewController implements IProjectReloadTask {

	private static final String DIGIT_POSITIV = "^[1-9]\\d*$";

	private Screen mainWindowScreen;
	private Alertable parentController; // Für Benachrichtungen

	@FXML private TextField pageCountTextField;
	@FXML private TextField columnTextField;
	@FXML private TextField rowTextField;

	public GeneralTabViewController(Screen currentScreen, Alertable parentController, boolean activePlayer) {
		super("generalTab", "de/tobias/playpad/assets/view/option/project/", PlayPadMain.getUiResourceBundle());

		this.mainWindowScreen = currentScreen;
		this.parentController = parentController;

		if (activePlayer) {
			rowTextField.setDisable(true);
			columnTextField.setDisable(true);
			pageCountTextField.setDisable(true);
		}

	}

	@Override
	public void init() {
		pageCountTextField.textProperty().addListener((a, b, c) ->
		{
			if (c.matches(DIGIT_POSITIV) && !c.isEmpty()) {
				int number = Integer.valueOf(c);
				if (number > ProjectSettings.MAX_PAGES) {
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
	}

	private boolean screenValid() {
		double width = mainWindowScreen.getVisualBounds().getMaxX() - mainWindowScreen.getVisualBounds().getMinX();
		double height = mainWindowScreen.getVisualBounds().getMaxY() - mainWindowScreen.getVisualBounds().getMinY();

		GlobalDesign layout = Profile.currentProfile().currentLayout();

		try {
			double neededWidth = layout.getMinWidth(Integer.valueOf(columnTextField.getText()));
			double neededHeight = layout.getMinHeight(Integer.valueOf(rowTextField.getText())) + 100;

			if (neededHeight <= height && neededWidth <= width)
				return true;
		} catch (NumberFormatException e) {
		}
		return false;
	}

	@Override
	public void loadSettings(ProjectSettings settings) {
		pageCountTextField.setText(String.valueOf(settings.getPageCount()));
		columnTextField.setText(String.valueOf(settings.getColumns()));
		rowTextField.setText(String.valueOf(settings.getRows()));

		if (screenValid()) {
			columnTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, false);
			rowTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, false);
		} else {
			columnTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true);
			rowTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true);
		}

	}

	private boolean changeSettings = false;

	@Override
	public void saveSettings(ProjectSettings settings) {
		int columns = Integer.valueOf(columnTextField.getText());
		int rows = Integer.valueOf(rowTextField.getText());
		int pageCount = Integer.valueOf(pageCountTextField.getText());

		if (settings.getColumns() != columns || settings.getRows() != rows || settings.getPageCount() != pageCount)
			changeSettings = true;
		else
			changeSettings = false;

		// Copy Settings
		settings.setColumns(columns);
		settings.setRows(rows);
		settings.setPageCount(pageCount);
	}

	@Override
	public boolean needReload() {
		return changeSettings;
	}

	@Override
	public boolean validSettings() {
		if (screenValid()) {
			return true;
		} else {
			double width = mainWindowScreen.getVisualBounds().getMaxX() - mainWindowScreen.getVisualBounds().getMinX();
			double height = mainWindowScreen.getVisualBounds().getMaxY() - mainWindowScreen.getVisualBounds().getMinY();

			GlobalDesign globalLayout = Profile.currentProfile().currentLayout();

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

	@Override
	public Task<Void> getTask(ProjectSettings settings, Project project, IMainViewController controller) {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				updateTitle(name());
				updateProgress(-1, -1);

				Platform.runLater(() ->
				{
					controller.getMenuToolbarController().initPageButtons();
					controller.createPadViews();
					controller.showPage(controller.getPage());
				});
				return null;
			}
		};
	}
}
