package de.tobias.playpad.viewcontroller.cell;

import java.nio.file.Path;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.plugin.PluginDescription;
import de.tobias.playpad.plugin.Plugins;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.Worker;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

public class PluginCell extends ListCell<PluginDescription> implements ChangeListener<Boolean> {

	private PluginDescription plugin;
	private HBox buttons;
	private CheckBox checkBox;

	public PluginCell() {
		checkBox = new CheckBox();
		checkBox.selectedProperty().addListener(this);

		buttons = new HBox(checkBox);
		buttons.setSpacing(14);
		buttons.setAlignment(Pos.CENTER_LEFT);
	}

	@Override
	protected void updateItem(PluginDescription item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty) {
			this.plugin = item;
			if (item.isActive()) {
				checkBox.setSelected(true);
			} else {
				checkBox.setSelected(false);
			}
			setGraphic(buttons);
			checkBox.setText(item.toString());
		} else {
			setGraphic(null);
		}
	}

	@Override
	public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		App app = ApplicationUtils.getApplication();

		Path path = app.getPath(PathType.LIBRARY, plugin.getFileName());
		if (newValue) { // Wurde Aktiviert
			Worker.runLater(() ->
			{
				Plugins.downloadPlugin(plugin, path);

				// Dependencies
				Plugins.loadDependencies(plugin);

				// Add Plugin to classpath
				Platform.runLater(() -> PlayPadMain.getProgramInstance().loadPlugin(path.toUri())); // FX Thread, damit Plugins GUI Zeug
																									// gleich auf dem richtigen Thread
																									// haben, sonst müssen sie den Worker
																									// nutzen
			});
		} else {
			// Deaktivieren
			PlayPadMain.getProgramInstance().addDeletedPlugin(path);
		}
	}
}
