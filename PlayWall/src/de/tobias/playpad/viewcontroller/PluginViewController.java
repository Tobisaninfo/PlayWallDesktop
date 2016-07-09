package de.tobias.playpad.viewcontroller;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.bukkit.configuration.MemorySection;

import de.tobias.playpad.AppUserInfoStrings;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.plugin.Plugin;
import de.tobias.playpad.plugin.Plugins;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.update.UpdateChannel;
import de.tobias.playpad.viewcontroller.cell.PluginCell;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.ui.ViewController;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Worker;
import javafx.application.Platform;
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
				String pluginInfoURL = null;

				MemorySection userInfo = ApplicationUtils.getApplication().getInfo().getUserInfo();
				UpdateChannel updateChannel = Profile.currentProfile().getProfileSettings().getUpdateChannel();

				if (updateChannel == UpdateChannel.STABLE) {
					pluginInfoURL = userInfo.getString(AppUserInfoStrings.PLUGINS_URL_STABLE);
				} else if (updateChannel == UpdateChannel.BETA) {
					pluginInfoURL = userInfo.getString(AppUserInfoStrings.PLUGINS_URL_BETA);
				} else {
					// No Plugins for this UpdateChannel Available --> Return
					showInfoMessage(Localization.getString(Strings.Error_Plugins_Available));
					return;
				}

				List<Plugin> plugins = Plugins.load(pluginInfoURL, true);

				Collections.sort(plugins);
				Platform.runLater(() ->
				{
					pluginListView.getItems().setAll(plugins);
				});
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