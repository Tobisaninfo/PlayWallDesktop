package de.tobias.playpad.viewcontroller.cell;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.plugin.Plugin;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import net.xeoh.plugins.base.PluginManager;

public class PluginCell extends ListCell<Plugin> implements ChangeListener<Boolean> {

	private Plugin plugin;
	private HBox buttons;
	private CheckBox checkBox;

	private PluginManager manager;

	public PluginCell(PluginManager manager) {
		this.manager = manager;

		checkBox = new CheckBox();
		checkBox.selectedProperty().addListener(this);

		buttons = new HBox(checkBox);
		buttons.setSpacing(14);
		buttons.setAlignment(Pos.CENTER_LEFT);
	}

	@Override
	protected void updateItem(Plugin item, boolean empty) {
		super.updateItem(item, empty);
		if (!empty) {
			this.plugin = item;
			if (item.isActive()) {
				checkBox.setSelected(true);
			} else {
				checkBox.setSelected(false);
			}
			setGraphic(buttons);
			checkBox.setText(item.getName());
		} else {
			setGraphic(null);
		}
	}

	@Override
	public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
		Path path = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, plugin.getFileName());
		if (newValue) { // Wurde Aktiviert
			if (Files.notExists(path)) {
				try {
					Files.createDirectories(path.getParent());
					Files.copy(new URL(plugin.getUrl()).openStream(), path);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			manager.addPluginsFrom(path.toUri());
		} else {
			PlayPadMain.addDeletedPlugin(path);
		}
	}
}
