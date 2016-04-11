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
import javafx.scene.control.TableCell;
import javafx.stage.Stage;

public class FixCell extends TableCell<PadException, PadException> {

	private Stage stage;

	public FixCell(Stage stage) {
		this.stage = stage;
	}

	@Override
	protected void updateItem(PadException item, boolean empty) {
		super.updateItem(item, empty);
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
								content.handlePath(path);
								pad.setName(FileUtils.getFilenameWithoutExtention(path.getFileName()));
								pad.removeException(item);
							}
						} catch (UnkownPadContentException e) {
							e.printStackTrace();
						}
					}
				});
				setGraphic(notExistsButton);
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
								content.handlePath(path);
								pad.setName(FileUtils.getFilenameWithoutExtention(path.getFileName()));
								pad.removeException(item);
							}
						} catch (UnkownPadContentException e) {
							e.printStackTrace();
						}
					}
				});
				setGraphic(supportButton);
				break;

			default:
				break;
			}
		} else {
			setGraphic(null);
		}
		setText("");
	}
}
