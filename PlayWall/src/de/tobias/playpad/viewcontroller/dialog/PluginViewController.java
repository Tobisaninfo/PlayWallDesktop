package de.tobias.playpad.viewcontroller.dialog;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import org.bukkit.configuration.MemorySection;

import de.tobias.playpad.AppUserInfoStrings;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.plugin.PluginDescription;
import de.tobias.playpad.plugin.Plugins;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.cell.PluginCell;
import de.tobias.updater.client.UpdateChannel;
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

public class PluginViewController extends NVC {

	@FXML private ListView<PluginDescription> pluginListView;
	@FXML private Button finishButton;

	public PluginViewController(Window owner) {
		this(owner, null);
	}

	public PluginViewController(Window owner, Set<Module> modules) {
		load("de/tobias/playpad/assets/dialog/", "pluginView", PlayPadMain.getUiResourceBundle());

		NVCStage nvcStage = applyViewControllerToStage();
		nvcStage.initOwner(owner);
		addCloseKeyShortcut(() -> getStageContainer().ifPresent(NVCStage::close));

		Worker.runLater(() ->
		{
			try {
				String pluginInfoURL = null;

				GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();
				UpdateChannel updateChannel = globalSettings.getUpdateChannel();

				MemorySection userInfo = ApplicationUtils.getApplication().getInfo().getUserInfo();
				if (updateChannel == UpdateChannel.STABLE) {
					pluginInfoURL = userInfo.getString(AppUserInfoStrings.PLUGINS_URL_STABLE);
				} else if (updateChannel == UpdateChannel.BETA) {
					pluginInfoURL = userInfo.getString(AppUserInfoStrings.PLUGINS_URL_BETA);
				} else {
					// No Plugins for this UpdateChannel Available --> Return
					showInfoMessage(Localization.getString(Strings.Error_Plugins_Available));
					return;
				}

				List<PluginDescription> plugins = Plugins.loadDescriptionFromServer(pluginInfoURL, true);

				Collections.sort(plugins);
				Platform.runLater(() ->
				{
					// Nur bestimmte Plugins zur Liste (die, die Fehlen)
					if (modules != null) {
						for (PluginDescription plugin : plugins) {
							for (Module module : modules) {
								if (module.identifier.equals(plugin.getId())) {
									pluginListView.getItems().add(plugin);
								}
							}
						}
					} else {
						// Alle Plugins zur Liste
						pluginListView.getItems().setAll(plugins);
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
				showErrorMessage(Localization.getString(Strings.Error_Standard_Gen), PlayPadMain.stageIcon);
			}
		});

	}

	@Override
	public void init() {
		pluginListView.setCellFactory(list -> new PluginCell());
		pluginListView.setPlaceholder(new Label(Localization.getString(Strings.UI_Placeholder_Plugins)));
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(550);
		stage.setMaxWidth(550);
		stage.setMinHeight(500);

		stage.setTitle(Localization.getString(Strings.UI_Dialog_Plugins_Title));
		stage.initModality(Modality.WINDOW_MODAL);

		if (Profile.currentProfile() != null)
			Profile.currentProfile().currentLayout().applyCss(stage);
	}

	@FXML
	private void finishButtonHandler(ActionEvent event) {
		getStageContainer().ifPresent(NVCStage::close);
	}
}