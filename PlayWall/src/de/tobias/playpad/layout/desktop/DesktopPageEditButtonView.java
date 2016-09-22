package de.tobias.playpad.layout.desktop;

import java.util.Optional;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.main.MenuToolbarViewController;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DesktopPageEditButtonView extends HBox implements EventHandler<ActionEvent> {

	private Page page;
	private Button leftMoveButton;
	private Button rightMoveButton;
	private Button editTextButton;
	private Button deleteButton;

	private transient Button pageButton;
	private transient MenuToolbarViewController controller;

	public DesktopPageEditButtonView(MenuToolbarViewController controller, Page page, Button pageButton) {
		this.page = page;
		this.pageButton = pageButton;
		this.controller = controller;

		leftMoveButton = new Button("", new FontIcon(FontAwesomeType.ARROW_LEFT));
		leftMoveButton.setOnAction(this);
		leftMoveButton.setFocusTraversable(false);

		rightMoveButton = new Button("", new FontIcon(FontAwesomeType.ARROW_RIGHT));
		rightMoveButton.setOnAction(this);
		rightMoveButton.setFocusTraversable(false);

		editTextButton = new Button("", new FontIcon(FontAwesomeType.EDIT));
		editTextButton.setOnAction(this);
		editTextButton.setFocusTraversable(false);

		deleteButton = new Button("", new FontIcon(FontAwesomeType.TRASH));
		deleteButton.setOnAction(this);
		deleteButton.setFocusTraversable(false);

		getChildren().addAll(leftMoveButton, rightMoveButton, editTextButton, deleteButton);
		setSpacing(7);
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() == leftMoveButton) {
			Project project = page.getProjectReference();
			if (page.getId() > 0) {
				Page leftPage = project.getPage(page.getId() - 1);

				int leftIndex = leftPage.getId();
				int rightIndex = page.getId();

				project.setPage(rightIndex, leftPage);
				project.setPage(leftIndex, page);

				IMainViewController controller = PlayPadPlugin.getImplementation().getMainViewController();
				if (controller.getMenuToolbarController() != null)
					controller.getMenuToolbarController().initPageButtons();
				controller.showPage(leftIndex);
			}
			event.consume();
		} else if (event.getSource() == rightMoveButton) {
			Project project = page.getProjectReference();
			if (page.getId() < project.getPages().size()) {
				Page rightPage = project.getPage(page.getId() + 1);

				int rightIndex = rightPage.getId();
				int leftIndex = page.getId();

				project.setPage(leftIndex, rightPage);
				project.setPage(rightIndex, page);

				IMainViewController controller = PlayPadPlugin.getImplementation().getMainViewController();
				if (controller.getMenuToolbarController() != null)
					controller.getMenuToolbarController().initPageButtons();
				controller.showPage(rightIndex);
			}
			event.consume();
		} else if (event.getSource() == editTextButton) {
			TextInputDialog dialog = new TextInputDialog();

			dialog.setHeaderText(Localization.getString(Strings.UI_Dialog_Page_Name_Header));
			dialog.setContentText(Localization.getString(Strings.UI_Dialog_Page_Name_Content));
			dialog.initOwner(controller.getStage());
			dialog.initModality(Modality.WINDOW_MODAL);
			Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
			PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

			Optional<String> result = dialog.showAndWait();

			result.filter(name -> name != null && !name.isEmpty()).ifPresent(name ->
			{
				page.setName(name);
			});

			// Update Page Button in Toolbar
			String name = page.getName();
			if (name.isEmpty())
				name = Localization.getString(Strings.UI_Window_Main_PageButton, (page.getId() + 1)); // Default Text
			pageButton.setText(name);

			event.consume();
		} else if (event.getSource() == deleteButton) {
			Alert alert = new Alert(AlertType.CONFIRMATION);

			alert.setHeaderText(Localization.getString(Strings.UI_Dialog_Page_Delete_Header));
			alert.setContentText(Localization.getString(Strings.UI_Dialog_Page_Delete_Content));
			alert.initOwner(controller.getStage());
			alert.initModality(Modality.WINDOW_MODAL);
			Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
			PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

			Optional<ButtonType> result = alert.showAndWait();
			System.out.println(result);
			result.filter(r -> r == ButtonType.OK).ifPresent(r ->
			{
				Project project = page.getProjectReference();
				project.removePage(page);
				controller.initPageButtons();
				controller.highlightPageButton(0); // Show first page
			});
		}
	}

}
