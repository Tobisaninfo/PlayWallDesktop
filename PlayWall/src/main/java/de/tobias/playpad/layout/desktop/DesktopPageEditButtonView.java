package de.tobias.playpad.layout.desktop;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.Project;
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

public class DesktopPageEditButtonView extends HBox implements EventHandler<ActionEvent> {

	private Page page;
	private Button leftMoveButton;
	private Button rightMoveButton;
	private Button editTextButton;
	private Button cloneButton;
	private Button deleteButton;

	private transient MenuToolbarViewController controller;
	private transient IMainViewController mainViewController;

	DesktopPageEditButtonView(MenuToolbarViewController controller, IMainViewController mainViewController, Page page) {
		this.page = page;

		this.controller = controller;
		this.mainViewController = mainViewController;

		leftMoveButton = new Button("", new FontIcon(FontAwesomeType.ARROW_LEFT));
		leftMoveButton.setOnAction(this);
		leftMoveButton.setTooltip(new Tooltip(Localization.getString(Strings.Tooltip_Page_LeftMove)));
		leftMoveButton.setFocusTraversable(false);

		rightMoveButton = new Button("", new FontIcon(FontAwesomeType.ARROW_RIGHT));
		rightMoveButton.setOnAction(this);
		rightMoveButton.setTooltip(new Tooltip(Localization.getString(Strings.Tooltip_Page_RightMove)));
		rightMoveButton.setFocusTraversable(false);

		editTextButton = new Button("", new FontIcon(FontAwesomeType.EDIT));
		editTextButton.setOnAction(this);
		editTextButton.setTooltip(new Tooltip(Localization.getString(Strings.Tooltip_Page_Rename)));
		editTextButton.setFocusTraversable(false);

		cloneButton = new Button("", new FontIcon(FontAwesomeType.COPY));
		cloneButton.setOnAction(this);
		cloneButton.setTooltip(new Tooltip(Localization.getString(Strings.Tooltip_Page_Clone)));
		cloneButton.setFocusTraversable(false);

		deleteButton = new Button("", new FontIcon(FontAwesomeType.TRASH));
		deleteButton.setOnAction(this);
		deleteButton.setTooltip(new Tooltip(Localization.getString(Strings.Tooltip_Page_Delete)));
		deleteButton.setFocusTraversable(false);

		getChildren().addAll(leftMoveButton, rightMoveButton, editTextButton, cloneButton, deleteButton);
		setSpacing(7);
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() == leftMoveButton) {
			Project project = page.getProject();
			if (page.getPosition() > 0) {
				Page leftPage = project.getPage(page.getPosition() - 1);

				int leftIndex = leftPage.getPosition();
				int rightIndex = page.getPosition();

				project.setPage(rightIndex, leftPage);
				project.setPage(leftIndex, page);

				IMainViewController controller = PlayPadPlugin.getInstance().getMainViewController();
				if (controller.getMenuToolbarController() != null)
					controller.getMenuToolbarController().initPageButtons();
				controller.showPage(leftIndex);
			}
			event.consume();
		} else if (event.getSource() == rightMoveButton) {
			Project project = page.getProject();
			if (page.getPosition() < project.getPages().size()) {
				Page rightPage = project.getPage(page.getPosition() + 1);

				int rightIndex = rightPage.getPosition();
				int leftIndex = page.getPosition();

				project.setPage(leftIndex, rightPage);
				project.setPage(rightIndex, page);

				IMainViewController controller = PlayPadPlugin.getInstance().getMainViewController();
				if (controller.getMenuToolbarController() != null)
					controller.getMenuToolbarController().initPageButtons();
				controller.showPage(rightIndex);
			}
			event.consume();
		} else if (event.getSource() == editTextButton) {
			showPageNameDialog(page);

			event.consume();
		} else if (event.getSource() == cloneButton) {
			try {
				Page clone = page.clone();

				// Show Rename dialog for cloned page
				boolean success = showPageNameDialog(clone);

				if (!success) {
					return;
				}

				Project project = page.getProject();
				project.addPage(clone);

				controller.initPageButtons();
				mainViewController.showPage(clone);
				event.consume();
			} catch (CloneNotSupportedException e) {
				Logger.error(e);
			}
		} else if (event.getSource() == deleteButton) {
			Alert alert = new Alert(AlertType.CONFIRMATION);

			alert.setHeaderText(Localization.getString(Strings.UI_Dialog_Page_Delete_Header));
			alert.setContentText(Localization.getString(Strings.UI_Dialog_Page_Delete_Content));
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
	}

	private boolean showPageNameDialog(Page page) {
		TextInputDialog dialog = new TextInputDialog(page.getName());

		dialog.setHeaderText(Localization.getString(Strings.UI_Dialog_Page_Name_Header));
		dialog.setContentText(Localization.getString(Strings.UI_Dialog_Page_Name_Content));
		dialog.initOwner(controller.getContainingWindow());
		dialog.initModality(Modality.WINDOW_MODAL);
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(page::setName);
		return result.isPresent();
	}

}
