package de.tobias.playpad.viewcontroller.cell.errordialog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadException;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.ContentFactory;
import de.tobias.playpad.pad.content.PadContentRegistry;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.view.ExceptionButton;
import de.tobias.utils.util.FileUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;

public class IOExceptionButtonListener implements EventHandler<ActionEvent> {

	private PadException item;
	private Stage stage;
	private ExceptionButton<Path> exButton;

	public IOExceptionButtonListener(PadException item, Stage stage, ExceptionButton<Path> notExistsExButton) {
		this.item = item;
		this.stage = stage;
		this.exButton = notExistsExButton;
	}

	@Override
	public void handle(ActionEvent a) {
		Path path = exButton.getHandler().handle(item.getPad(), stage);
		if (path != null) {

			Pad pad = item.getPad();
			PadContent content = item.getPad().getContent();
			try {
				PadContentRegistry padContents = PlayPadPlugin.getRegistryCollection().getPadContents();
				ContentFactory contentFactory = padContents.getFactory(content.getType());

				if (Files.exists(path) && ContentFactory.isFileTypeSupported(path, contentFactory)) {
					content.handlePath(path);
					pad.setName(FileUtils.getFilenameWithoutExtention(path.getFileName()));
					pad.removeException(item);
				}
			} catch (NoSuchComponentException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
