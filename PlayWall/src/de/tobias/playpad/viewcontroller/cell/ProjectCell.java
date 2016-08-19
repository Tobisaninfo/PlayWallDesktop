package de.tobias.playpad.viewcontroller.cell;

import java.nio.file.Files;
import java.nio.file.Path;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.project.ProjectReference;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;

public class ProjectCell extends ListCell<ProjectReference> {

	private Displayable ref;

	@Override
	protected void updateItem(ProjectReference ref, boolean empty) {
		super.updateItem(ref, empty);
		if (!empty) {
			if (this.ref == null || this.ref != ref) {
				Path path = ref.getProjectPath();
				if (Files.notExists(path) || !ref.getMissedModules().isEmpty()) {
					FontIcon graphics = new FontIcon(FontAwesomeType.WARNING);
					graphics.setColor(Color.RED);
					setGraphic(graphics);
				}
				setContentDisplay(ContentDisplay.RIGHT);

				textProperty().bind(ref.displayProperty());
				this.ref = ref;
			}
		} else {
			this.ref = null;
			textProperty().unbind();

			setGraphic(null);
			setText("");
		}
	}
}
