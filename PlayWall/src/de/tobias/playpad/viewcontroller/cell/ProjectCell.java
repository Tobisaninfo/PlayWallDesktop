package de.tobias.playpad.viewcontroller.cell;

import java.nio.file.Files;
import java.nio.file.Path;

import de.tobias.playpad.Displayable;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.server.ConnectionState;
import de.tobias.playpad.server.Server;
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

	private transient Displayable ref;

	private boolean showProfileName;

	public ProjectCell(boolean showProfileName) {
		this.showProfileName = showProfileName;
	}

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

				if (showProfileName) {
					// Profile name
					ProfileReference profileRef = ref.getProfileReference();
					if (profileRef != null) {
						String name = profileRef.getName();

						Label label = new Label(name);
						label.getStyleClass().add("profilename");
						nameBox.getChildren().add(label);
					}
				}

				HBox.setHgrow(nameBox, Priority.ALWAYS);
				rootBox.getChildren().add(nameBox);

				FontIcon cloudGraphics = new FontIcon(FontAwesomeType.CLOUD);
				cloudGraphics.visibleProperty().bind(ref.syncProperty());
				rootBox.getChildren().add(cloudGraphics);

				// File not Exists
				Path path = ref.getProjectPath();
				Server server = PlayPadPlugin.getServerHandler().getServer();
				if ((Files.notExists(path) && !ref.isSync()) || !ref.getMissedModules().isEmpty() ||
						(Files.notExists(path) && ref.isSync() && server.getConnectionState() == ConnectionState.CONNECTION_LOST)) {
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
