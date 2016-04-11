package de.tobias.playpad.viewcontroller;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import de.tobias.playpad.AppUserInfoStrings;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.plugin.Plugin;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.cell.PluginCell;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import net.xeoh.plugins.base.PluginManager;

public class PluginViewController extends ViewController {

	@FXML private ListView<Plugin> pluginListView;
	@FXML private Button finishButton;

	private PluginManager manager;

	public PluginViewController(PluginManager manager, Window owner) {
		super("pluginView", "de/tobias/playpad/assets/dialog/", null, PlayPadMain.getUiResourceBundle());
		this.manager = manager;

		getStage().initOwner(owner);
		getStage().initModality(Modality.WINDOW_MODAL);

		Worker.runLater(() ->
		{
			try {
				List<Plugin> plugins = new ArrayList<>();

				URL url = new URL(ApplicationUtils.getApplication().getInfo().getUserInfo().getString(AppUserInfoStrings.PLUGINS_URL));
				FileConfiguration cfg = YamlConfiguration.loadConfiguration(url.openStream());

				// Iterate over all plugins that are online avialable
				for (String key : cfg.getConfigurationSection("plugins").getKeys(false)) {
					String name = cfg.getString("plugins." + key + ".name");
					String pluginUrl = cfg.getString("plugins." + key + ".url");
					String fileName = cfg.getString("plugins." + key + ".filename");
					boolean active = false;

					try {
						Path path = ApplicationUtils.getApplication().getPath(PathType.LIBRARY, fileName);
						if (Files.exists(path))
							active = true;
					} catch (Exception e) {
						e.printStackTrace();
						showErrorMessage(Localization.getString(Strings.Error_Plugins_Download, name), PlayPadMain.stageIcon);
					}

					Plugin plugin = new Plugin(name, fileName, pluginUrl, active);
					plugins.add(plugin);
				}

				Collections.sort(plugins, (o1, o2) -> o1.getName().compareTo(o2.getName()));
				pluginListView.getItems().addAll(plugins);
			} catch (IOException e) {
				e.printStackTrace();
				showErrorMessage(Localization.getString(Strings.Error_Standard_Gen), PlayPadMain.stageIcon);
			}
		});
	}

	@Override
	public void init() {
		pluginListView.setCellFactory(list -> new PluginCell(manager));
		pluginListView.setPlaceholder(new Label(Localization.getString(Strings.UI_Placeholder_Plugins)));

		addCloseKeyShortcut(() -> getStage().close());
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(550);
		stage.setMaxWidth(550);
		stage.setMinHeight(500);

		stage.setTitle(Localization.getString(Strings.UI_Dialog_Plugins_Title));

		Profile.currentProfile().currentLayout().applyCss(getStage());
	}

	@FXML
	private void finishButtonHandler(ActionEvent event) {
		getStage().close();
	}
}