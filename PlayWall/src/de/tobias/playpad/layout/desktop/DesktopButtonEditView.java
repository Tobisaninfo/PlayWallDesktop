package de.tobias.playpad.layout.desktop;

import java.util.Optional;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.project.v2.ProjectV2;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;

public class DesktopButtonEditView extends HBox implements EventHandler<ActionEvent> {

	private Page page;
	private Button leftMoveButton;
	private Button rightMoveButton;
	private Button editTextButton;

	private transient Button pageButton;

	public DesktopButtonEditView(Page page, Button pageButton) {
		this.page = page;
		this.pageButton = pageButton;

		leftMoveButton = new Button("", new FontIcon(FontAwesomeType.ARROW_LEFT));
		leftMoveButton.setOnAction(this);
		leftMoveButton.setFocusTraversable(false);

		rightMoveButton = new Button("", new FontIcon(FontAwesomeType.ARROW_RIGHT));
		rightMoveButton.setOnAction(this);
		rightMoveButton.setFocusTraversable(false);

		editTextButton = new Button("", new FontIcon(FontAwesomeType.EDIT));
		editTextButton.setOnAction(this);
		editTextButton.setFocusTraversable(false);

		getChildren().addAll(leftMoveButton, rightMoveButton, editTextButton);
		setSpacing(7);
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() == leftMoveButton) {
			ProjectV2 project = page.getProjectReference();
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
			ProjectV2 project = page.getProjectReference();
			if (page.getId() < project.getSettings().getPageCount()) {
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
			Optional<String> result = dialog.showAndWait();
			// TODO Owner, Modal, Icon, Text
			result.ifPresent(name ->
			{
				page.setName(name);
			});

			String name = page.getName();
			if (name.isEmpty()) {
				name = Localization.getString(Strings.UI_Window_Main_PageButton, (page.getId() + 1));
			}
			pageButton.setText(name);
			
			event.consume();
		}
	}

}
