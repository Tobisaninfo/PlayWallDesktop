package de.tobias.playpad.viewcontroller.cell;

import java.nio.file.Files;
import java.nio.file.Path;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.project.ProjectReference;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;

public class ProjectCell extends ListCell<ProjectReference> {

	private Displayable action;

	@Override
	protected void updateItem(ProjectReference action, boolean empty) {
		super.updateItem(action, empty);
		if (!empty) {
			if (this.action == null || this.action != action) {
				Path path = ApplicationUtils.getApplication().getPath(PathType.DOCUMENTS, action.getFileName());
				if (Files.notExists(path)) {
					FontIcon graphics = new FontIcon(FontAwesomeType.WARNING);
					graphics.setColor(Color.RED);
					setGraphic(graphics);
				}
				setContentDisplay(ContentDisplay.RIGHT);

				textProperty().bind(action.displayProperty());
				this.action = action;
			}
		} else {
			this.action = null;
			textProperty().unbind();

			setGraphic(null);
			setText("");
		}
	}
}
