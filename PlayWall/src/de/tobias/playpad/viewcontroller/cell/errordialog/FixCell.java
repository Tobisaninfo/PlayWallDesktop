package de.tobias.playpad.viewcontroller.cell.errordialog;

import java.nio.file.Path;

import de.tobias.playpad.pad.PadException;
import de.tobias.playpad.view.ExceptionButton;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// TODO Rewrite --> Extract Button Listeners
public class FixCell extends TableCell<PadException, PadException> {

	private Stage stage;
	private VBox vbox;

	public FixCell(Stage stage) {
		this.stage = stage;
	}

	@Override
	protected void updateItem(PadException item, boolean empty) {
		super.updateItem(item, empty);

		vbox = new VBox();
		if (!empty) {
			switch (item.getType()) {
			case FILE_NOT_FOUND:
				ExceptionButton<Path> notExistsExButton = ExceptionButton.FILE_NOT_FOUND_EXCEPTION;
				Button notExistsButton = notExistsExButton.getButton();
				notExistsButton.setOnAction(new IOExceptionButtonListener(item, stage, notExistsExButton));
				vbox.getChildren().add(notExistsButton);
				break;

			case FILE_FORMAT_NOT_SUPPORTED:
			case CONVERT_NOT_SUPPORTED:
				ExceptionButton<Path> supportExButton = ExceptionButton.FILE_NOT_FOUND_EXCEPTION;
				Button supportButton = supportExButton.getButton();
				supportButton.setOnAction(new IOExceptionButtonListener(item, stage, supportExButton));
				vbox.getChildren().add(supportButton);
				break;

			default:
				break;
			}

			ExceptionButton<Path> deleteExButton = ExceptionButton.DELETE_EXCEPTION;
			Button deleteButton = deleteExButton.getButton();
			deleteButton.setOnAction(a ->
			{
				deleteExButton.getHandler().handle(item.getPad(), stage);
				item.getPad().getProject().removeException(item);
			});
			vbox.getChildren().add(deleteButton);

			vbox.setSpacing(7);
			vbox.getChildren().forEach(node ->
			{
				if (node instanceof Control)
					((Control) node).setMaxWidth(Double.MAX_VALUE);
				VBox.setVgrow(node, Priority.ALWAYS);
			});

			setGraphic(vbox);
		} else {
			setGraphic(null);
		}
		setText("");
	}
}
