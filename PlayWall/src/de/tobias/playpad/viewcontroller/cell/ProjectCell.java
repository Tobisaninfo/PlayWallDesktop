package de.tobias.playpad.viewcontroller.cell;

import java.nio.file.Files;
import java.nio.file.Path;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.project.ProjectReference;
import de.tobias.playpad.settings.ProfileReference;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class ProjectCell extends ListCell<ProjectReference> {

	private Displayable ref;

	@Override
	protected void updateItem(ProjectReference ref, boolean empty) {
		super.updateItem(ref, empty);
		if (!empty) {
			if (this.ref == null || this.ref != ref) {
				HBox rootBox = new HBox(14);
				VBox nameBox = new VBox(3);

				// init
				rootBox.setAlignment(Pos.CENTER_LEFT);

				// Project Name
				Label projectNameLabel = new Label();
				projectNameLabel.textProperty().bind(ref.displayProperty());
				projectNameLabel.getStyleClass().add("projectname");
				nameBox.getChildren().add(projectNameLabel);

				// Profile name
				ProfileReference profileRef = ref.getProfileReference();
				if (profileRef != null) {
					String name = profileRef.getName();

					Label label = new Label(name);
					label.getStyleClass().add("profilename");
					nameBox.getChildren().add(label);
				}

				HBox.setHgrow(nameBox, Priority.ALWAYS);
				rootBox.getChildren().add(nameBox);

				// File not Exists
				Path path = ref.getProjectPath();
				if (Files.notExists(path)) {
					FontIcon graphics = new FontIcon(FontAwesomeType.WARNING);
					graphics.setColor(Color.RED);
					rootBox.getChildren().add(graphics);
				}

				setGraphic(rootBox);
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
