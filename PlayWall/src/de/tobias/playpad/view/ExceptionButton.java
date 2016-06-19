package de.tobias.playpad.view;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.util.Localization;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class ExceptionButton<T> {

	public static ExceptionButton<Path> FILE_NOT_FOUND_EXCEPTION = new ExceptionButton<>(Localization.getString(Strings.Error_Fix_NewFile),
			new Handler<Path>() {

				@Override
				public Path handle(Pad pad, Window owner) {
					FileChooser chooser = new FileChooser();

					Object lastFolder = ApplicationUtils.getApplication().getUserDefaults().getData("openFolder");
					if (lastFolder != null) {
						Path path = Paths.get(lastFolder.toString());
						chooser.setInitialDirectory(path.toFile());
					}
					File file = chooser.showOpenDialog(owner);
					return file != null ? file.toPath() : null;
				}
			});
	
	public static ExceptionButton<Path> DELETE_EXCEPTION = new ExceptionButton<>(Localization.getString(Strings.Error_Fix_Delete),
			new Handler<Path>() {

				@Override
				public Path handle(Pad pad, Window owner) {
					pad.clear();
					return null;
				}
			});

	public static interface Handler<T> {

		public T handle(Pad pad, Window owner);
	}

	private String title;
	private Handler<T> handler;

	public ExceptionButton(String title, Handler<T> handler) {
		this.title = title;
		this.handler = handler;
	}

	public Button getButton() {
		return new Button(title);
	}

	public Handler<T> getHandler() {
		return handler;
	}
}
