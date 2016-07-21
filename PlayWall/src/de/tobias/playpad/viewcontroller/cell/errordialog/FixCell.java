package de.tobias.playpad.viewcontroller.cell.errordialog;

import java.nio.file.Files;
import java.nio.file.Path;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadException;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.pad.conntent.PadContentRegistry;
import de.tobias.playpad.pad.conntent.UnkownPadContentException;
import de.tobias.playpad.view.ExceptionButton;
import de.tobias.utils.util.FileUtils;
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
				notExistsButton.setOnAction(a ->
				{
					Path path = notExistsExButton.getHandler().handle(item.getPad(), stage);
					if (path != null) {

						Pad pad = item.getPad();
						PadContent content = item.getPad().getContent();
						try {
							PadContentConnect padContentConnect = PadContentRegistry.getPadContentConnect(content.getType());

							if (Files.exists(path) && PadContentConnect.isFileSupported(path, padContentConnect)) {
								try {
									content.handlePath(path);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								pad.setName(FileUtils.getFilenameWithoutExtention(path.getFileName()));
								pad.removeException(item);
							}
						} catch (UnkownPadContentException e) {
							e.printStackTrace();
						}
					}
				});
				vbox.getChildren().add(notExistsButton);
				break;

			case FILE_FORMAT_NOT_SUPPORTED:
			case CONVERT_NOT_SUPPORTED:
				ExceptionButton<Path> supportExButton = ExceptionButton.FILE_NOT_FOUND_EXCEPTION;
				Button supportButton = supportExButton.getButton();
				supportButton.setOnAction(a ->
				{
					Path path = supportExButton.getHandler().handle(item.getPad(), stage);
					if (path != null) {

						Pad pad = item.getPad();
						PadContent content = item.getPad().getContent();
						try {
							PadContentConnect padContentConnect = PadContentRegistry.getPadContentConnect(content.getType());

							if (Files.exists(path) && PadContentConnect.isFileSupported(path, padContentConnect)) {
								try {
									content.handlePath(path);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								pad.setName(FileUtils.getFilenameWithoutExtention(path.getFileName()));
								pad.removeException(item);
							}
						} catch (UnkownPadContentException e) {
							e.printStackTrace();
						}
					}
				});
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
