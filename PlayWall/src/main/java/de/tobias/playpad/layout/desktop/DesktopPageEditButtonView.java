package de.tobias.playpad.layout.desktop;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.icon.FontIconType;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

public class DesktopPageEditButtonView extends HBox {

	private static class EditButton extends Button {
		private EditButton(FontIconType icon, EventHandler<ActionEvent> onAction, String tooltip) {
			super("", new FontIcon(icon));
			setOnAction(onAction);
			setTooltip(new Tooltip(tooltip));
			setFocusTraversable(false);
		}
	}

	private final Page page;

	private final transient MenuToolbarViewController controller;
	private final transient IMainViewController mainViewController;

	DesktopPageEditButtonView(MenuToolbarViewController controller, IMainViewController mainViewController, Page page) {
		this.page = page;

		this.controller = controller;
		this.mainViewController = mainViewController;

		Button leftMoveButton = new EditButton(FontAwesomeType.ARROW_LEFT, this::onLeftButton, Localization.getString(Strings.TOOLTIP_PAGE_LEFT_MOVE));
		Button rightMoveButton = new EditButton(FontAwesomeType.ARROW_RIGHT, this::onRightButton, Localization.getString(Strings.TOOLTIP_PAGE_RIGHT_MOVE));
		Button editTextButton = new EditButton(FontAwesomeType.EDIT, this::onRenameButton, Localization.getString(Strings.TOOLTIP_PAGE_RENAME));
		Button cloneButton = new EditButton(FontAwesomeType.COPY, this::onCloneButton, Localization.getString(Strings.TOOLTIP_PAGE_CLONE));
		Button deleteButton = new EditButton(FontAwesomeType.TRASH, this::onDeleteButton, Localization.getString(Strings.TOOLTIP_PAGE_DELETE));

		getChildren().addAll(leftMoveButton, rightMoveButton, editTextButton, cloneButton, deleteButton);
		setSpacing(7);
	}

	private void onLeftButton(ActionEvent event) {
		final Project project = page.getProject();
		if (page.getPosition() > 0) {
			final Page leftPage = project.getPage(page.getPosition() - 1);

			final int leftIndex = leftPage.getPosition();
			final int rightIndex = page.getPosition();

			project.setPage(rightIndex, leftPage);
			project.setPage(leftIndex, page);

			if (mainViewController.getMenuToolbarController() != null)
				mainViewController.getMenuToolbarController().initPageButtons();
			mainViewController.showPage(leftIndex);
		}
		event.consume();
	}

	private void onRightButton(ActionEvent event) {
		final Project project = page.getProject();
		if (page.getPosition() < project.getPages().size()) {
			final Page rightPage = project.getPage(page.getPosition() + 1);

			final int rightIndex = rightPage.getPosition();
			final int leftIndex = page.getPosition();

			project.setPage(leftIndex, rightPage);
			project.setPage(rightIndex, page);

			if (mainViewController.getMenuToolbarController() != null)
				mainViewController.getMenuToolbarController().initPageButtons();
			mainViewController.showPage(rightIndex);
		}
		event.consume();
	}

	private void onRenameButton(ActionEvent event) {
		showPageNameDialog(page);
	}

	private void onCloneButton(ActionEvent event) {
		final Page clone = page.copy();

		// Show Rename dialog for cloned page
		boolean success = showPageNameDialog(clone);

		if (!success) {
			return;
		}

		Project project = page.getProject();
		boolean added = project.addPage(clone);

		if (!added) {
			mainViewController.showErrorMessage(Localization.getString(Strings.ERROR_PROJECT_PAGE_COUNT, ProjectSettings.MAX_PAGES));
			return;
		}

		controller.initPageButtons();
		mainViewController.showPage(clone);
	}

	private void onDeleteButton(ActionEvent event) {
		Alert alert = new Alert(AlertType.CONFIRMATION);

		alert.setHeaderText(Localization.getString(Strings.UI_DIALOG_PAGE_DELETE_HEADER));
		alert.setContentText(Localization.getString(Strings.UI_DIALOG_PAGE_DELETE_CONTENT));
		alert.initOwner(controller.getContainingWindow());
		alert.initModality(Modality.WINDOW_MODAL);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

		Optional<ButtonType> result = alert.showAndWait();
		result.filter(r -> r == ButtonType.OK).ifPresent(r ->
		{
			Project project = page.getProject();
			project.removePage(page);
			PlayPadMain.getProgramInstance().getMainViewController().showPage(0);
			controller.initPageButtons();
			controller.highlightPageButton(0); // Show first page
			event.consume();
		});
	}

	private boolean showPageNameDialog(Page page) {
		TextInputDialog dialog = new TextInputDialog(page.getName());

		dialog.setHeaderText(Localization.getString(Strings.UI_DIALOG_PAGE_NAME_HEADER));
		dialog.setContentText(Localization.getString(Strings.UI_DIALOG_PAGE_NAME_CONTENT));
		dialog.initOwner(controller.getContainingWindow());
		dialog.initModality(Modality.WINDOW_MODAL);
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(page::setName);
		return result.isPresent();
	}

}
