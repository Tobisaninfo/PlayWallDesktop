package de.tobias.playpad.viewcontroller.dialog;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.plugin.ModernPlugin;
import de.tobias.playpad.plugin.ModernPluginManager;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.settings.Profile;
import de.tobias.utils.nui.NVC;
import de.tobias.utils.nui.NVCStage;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Worker;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by tobias on 08.02.17.
 */
public class ModernPluginViewController extends NVC implements ChangeListener<ModernPlugin> {

	@FXML
	private TextField searchField;
	@FXML
	private Label pluginVersionLabel;
	@FXML
	private Label pluginHeadlineLabel;
	@FXML
	private Label pluginInfoLabel;
	@FXML
	private Button pluginInstallButton;

	@FXML
	private ListView<ModernPlugin> pluginList;

	public ModernPluginViewController(Window owner) {
		loadView(owner);
		Worker.runLater(() -> {
			try {
				List<ModernPlugin> plugins = PlayPadPlugin.getServerHandler().getServer().getPlugins();
				Platform.runLater(() -> pluginList.getItems().setAll(plugins));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public ModernPluginViewController(Window owner, Set<Module> missedModules) {
		loadView(owner);
		Worker.runLater(() -> {
			try {
				List<ModernPlugin> plugins = PlayPadPlugin.getServerHandler().getServer().getPlugins().stream()
						.filter(p -> missedModules.parallelStream().anyMatch(m -> m.identifier.equals(p.getName())))
						.collect(Collectors.toList());

				Platform.runLater(() -> pluginList.getItems().setAll(plugins));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private void loadView(Window owner) {
		load("de/tobias/playpad/assets/view/dialogs/", "pluginView", PlayPadMain.getUiResourceBundle());
		NVCStage stage = applyViewControllerToStage();
		stage.initOwner(owner);
		stage.initModality(Modality.WINDOW_MODAL);
		addCloseKeyShortcut(stage::close);

	}

	@Override
	public void init() {
		pluginList.getSelectionModel().selectedItemProperty().addListener(this);
	}

	@Override
	public void initStage(Stage stage) {
		PlayPadMain.stageIcon.ifPresent(stage.getIcons()::add);

		stage.setMinWidth(600);
		stage.setMinHeight(400);
		stage.setTitle(Localization.getString(Strings.UI_Dialog_Plugins_Title));

		if (Profile.currentProfile() != null) {
			Profile.currentProfile().currentLayout().applyCss(stage);
		} else {
			// Add Stylesheet manuel
			stage.getScene().getStylesheets().add("de/tobias/playpad/assets/style.css");
		}
	}

	@Override
	public void changed(ObservableValue<? extends ModernPlugin> observable, ModernPlugin oldValue, ModernPlugin newValue) {
		pluginHeadlineLabel.setText(newValue.getDisplayName());
		pluginInfoLabel.setText(newValue.getDescription());
		pluginVersionLabel.setText(newValue.getVersion());
	}

	@FXML
	private void pluginInstallHandler(ActionEvent event) {
		ModernPlugin plugin = pluginList.getSelectionModel().getSelectedItem();
		if (plugin != null) {
			if (!ModernPluginManager.getInstance().isActive(plugin)) {
				try {
					GlobalSettings settings = PlayPadPlugin.getImplementation().getGlobalSettings();
					PlayPadPlugin.getServerHandler().getServer().loadPlugin(plugin, settings.getUpdateChannel());
					ModernPluginManager.getInstance().loadPlugin(plugin);
				} catch (IOException e) {
					e.printStackTrace(); // TODO Error handling
				}
			} else {
				ModernPluginManager.getInstance().unloadPlugin(plugin);
			}
		}
	}
}
