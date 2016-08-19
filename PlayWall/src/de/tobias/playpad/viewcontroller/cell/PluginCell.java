package de.tobias.playpad.viewcontroller.cell;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.plugin.PluginDescription;
import de.tobias.playpad.plugin.Plugins;
import de.tobias.utils.application.App;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
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
			downloadPlugin(plugin, path);

			// Dependencies
			loadDependencies(app);

			// Add Plugin to classpath
			PlayPadMain.getProgramInstance().loadPlugin(path.toUri());
		} else {
			// Deaktivieren
			PlayPadMain.getProgramInstance().addDeletedPlugin(path);
		}
	}

	private void loadDependencies(App app) {
		List<PluginDescription> dependencies = findDependencies();
		dependencies.forEach(p ->
		{
			Path decPath = app.getPath(PathType.LIBRARY, p.getFileName());
			downloadPlugin(p, decPath);

			// Add Plugin to classpath
			PlayPadMain.getProgramInstance().loadPlugin(decPath.toUri());
		});
	}

	private List<PluginDescription> findDependencies() {
		List<PluginDescription> plugins = new ArrayList<>();
		for (String dependencyName : plugin.getDependencies()) {
			for (PluginDescription plugin : Plugins.getAvailablePlugins()) {
				if (plugin.getName().equals(dependencyName)) {
					plugins.add(plugin);
				}
			}
		}
		return plugins;
	}

	private void downloadPlugin(PluginDescription plugin, Path path) {
		if (Files.notExists(path)) {
			try {
				Files.createDirectories(path.getParent());
				Files.copy(new URL(plugin.getUrl()).openStream(), path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
