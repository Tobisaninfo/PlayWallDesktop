package de.tobias.playpad.viewcontroller.option.project;

import de.thecodelabs.utils.ui.Alertable;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.Strings;
import de.tobias.playpad.design.ModernDesignSizeHelper;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.project.ProjectSettingsValidator;
import de.tobias.playpad.project.ProjectSettingsValidator.ValidationState;
import de.tobias.playpad.view.PseudoClasses;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.IProjectReloadTask;
import de.tobias.playpad.viewcontroller.option.ProjectSettingsTabViewController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Screen;

import static de.tobias.playpad.project.ProjectSettingsValidator.Dimension;

public class GeneralTabViewController extends ProjectSettingsTabViewController implements IProjectReloadTask {

	private static final String DIGIT_POSITIVE = "^[1-9]\\d*$";

	private final Screen mainWindowScreen;
	private final Alertable parentController; // Für Benachrichtungen

	private final ProjectSettingsValidator projectSettingsValidator;

	@FXML
	private TextField columnTextField;
	@FXML
	private TextField rowTextField;

	@FXML
	private Label columnErrorLabel;
	@FXML
	private Label rowErrorLabel;

	GeneralTabViewController(Screen currentScreen, Alertable parentController, boolean activePlayer) {
		load("view/option/project", "GeneralTab", Localization.getBundle());

		this.mainWindowScreen = currentScreen;
		this.parentController = parentController;

		this.projectSettingsValidator = new ProjectSettingsValidator(this.mainWindowScreen);

		if (activePlayer) {
			rowTextField.setDisable(true);
			columnTextField.setDisable(true);
		}
	}

	@Override
	public void init() {
		columnTextField.textProperty().addListener((a, b, c) ->
		{
			if (c.matches(DIGIT_POSITIVE) && !c.isEmpty()) {
				ValidationState validationState = projectSettingsValidator.validate(Integer.parseInt(c), Dimension.COLUMNS);
				if (validationState == ValidationState.NORMAL) {
					columnTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, false);
					columnErrorLabel.setText("");
				} else {
					columnTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true);

					String errorText = "";
					// Error Message
					if (validationState == ValidationState.TOO_MUCH) {
						errorText = Localization.getString(Strings.ERROR_SCREEN_TOO_MUCH, projectSettingsValidator.maxValue(Dimension.COLUMNS));
					} else if (validationState == ValidationState.TOO_LESS) {
						errorText = Localization.getString(Strings.ERROR_SCREEN_TOO_LESS, projectSettingsValidator.minValue(Dimension.COLUMNS));
					}
					columnErrorLabel.setText(errorText);
				}
			} else {
				columnTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true);
				columnErrorLabel.setText(Localization.getString(Strings.ERROR_SCREEN_TOO_LESS, projectSettingsValidator.minValue(Dimension.COLUMNS)));
			}
		});

		rowTextField.textProperty().addListener((a, b, c) ->
		{
			if (c.matches(DIGIT_POSITIVE) && !c.isEmpty()) {
				ValidationState validationState = projectSettingsValidator.validate(Integer.parseInt(c), Dimension.ROWS);
				if (validationState == ValidationState.NORMAL) {
					rowTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, false);
					rowErrorLabel.setText("");
				} else {
					rowTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true);

					String errorText = "";
					// Error Message
					if (validationState == ValidationState.TOO_MUCH) {
						errorText = Localization.getString(Strings.ERROR_SCREEN_TOO_MUCH, projectSettingsValidator.maxValue(Dimension.ROWS));
					} else if (validationState == ValidationState.TOO_LESS) {
						errorText = Localization.getString(Strings.ERROR_SCREEN_TOO_LESS, projectSettingsValidator.minValue(Dimension.ROWS));
					}
					rowErrorLabel.setText(errorText);
				}
			} else {
				rowTextField.pseudoClassStateChanged(PseudoClasses.ERROR_CLASS, true);
				rowErrorLabel.setText(Localization.getString(Strings.ERROR_SCREEN_TOO_LESS, projectSettingsValidator.minValue(Dimension.ROWS)));
			}
		});
	}

	private boolean screenValid() {
		double width = mainWindowScreen.getVisualBounds().getMaxX() - mainWindowScreen.getVisualBounds().getMinX();
		double height = mainWindowScreen.getVisualBounds().getMaxY() - mainWindowScreen.getVisualBounds().getMinY();

		try {
			int column = Integer.parseInt(columnTextField.getText());
			int rows = Integer.parseInt(rowTextField.getText());

			if (column < 3 || rows < 1) {
				return false;
			}

			double neededWidth = ModernDesignSizeHelper.getMinWidth(column);
			double neededHeight = ModernDesignSizeHelper.getMinHeight(rows) + 100;

			if (neededHeight <= height && neededWidth <= width)
				return true;
		} catch (NumberFormatException ignored) {
		}
		return false;
	}

	@Override
	public void loadSettings(ProjectSettings settings) {
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
		int columns = Integer.parseInt(columnTextField.getText());
		int rows = Integer.parseInt(rowTextField.getText());

		changeSettings = settings.getColumns() != columns || settings.getRows() != rows;

		// Copy Settings
		settings.setColumns(columns);
		settings.setRows(rows);
	}

	@Override
	public boolean needReload() {
		return changeSettings;
	}

	@Override
	public boolean validSettings() {
		try {
			return projectSettingsValidator.validate(Integer.parseInt(columnTextField.getText()), Dimension.COLUMNS) == ValidationState.NORMAL &&
					projectSettingsValidator.validate(Integer.parseInt(rowTextField.getText()), Dimension.ROWS) == ValidationState.NORMAL;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public String name() {
		return Localization.getString(Strings.UI_WINDOW_SETTINGS_GEN_TITLE);
	}

	@Override
	public Runnable getTask(ProjectSettings settings, Project project, IMainViewController controller) {
		return () -> Platform.runLater(() ->
		{
			controller.getMenuToolbarController().initPageButtons();
			controller.createPadViews();
			controller.showPage(controller.getPage());
		});
	}
}
