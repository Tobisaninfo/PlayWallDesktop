package de.tobias.playpad.viewcontroller.dialog;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.threading.Worker;
import de.thecodelabs.utils.ui.Alerts;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.plugin.ModernPlugin;
import de.tobias.playpad.plugin.ModernPluginManager;
import de.tobias.playpad.plugin.Module;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.settings.GlobalSettings;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

	private ObservableList<ModernPlugin> data = FXCollections.observableArrayList();
	private FilteredList<ModernPlugin> filteredData = new FilteredList<>(data, s -> true);


	public ModernPluginViewController(Window owner) {
		loadView(owner);
		Worker.runLater(() -> {
			try {
				List<ModernPlugin> plugins = PlayPadPlugin.getServerHandler().getServer().getPlugins();
				Platform.runLater(() -> {
					data.addAll(plugins);
					pluginList.getSelectionModel().selectFirst();
				});
			} catch (IOException e) {
				Logger.error(e);
				Platform.runLater(() -> {
					Alerts.getInstance()
							.createAlert(Alert.AlertType.ERROR,
									Localization.getString(Strings.Error_Plugins_Header),
									Localization.getString(Strings.Error_Plugins_Loading),
									getContainingWindow()
							).showAndWait();
					closeStage();
				});
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

				Platform.runLater(() -> {
					data.addAll(plugins);
					pluginList.getSelectionModel().selectFirst();
				});
			} catch (IOException e) {
				Logger.error(e);
				Platform.runLater(() -> {
					Alerts.getInstance()
							.createAlert(Alert.AlertType.ERROR,
									Localization.getString(Strings.Error_Plugins_Header),
									Localization.getString(Strings.Error_Plugins_Loading),
									getContainingWindow()
							).showAndWait();
					closeStage();
				});
			}
		});
	}

	private void loadView(Window owner) {
		load("view/dialog", "PluginDialog", Localization.getBundle());
		NVCStage stage = applyViewControllerToStage();
		stage.initOwner(owner);
		stage.initModality(Modality.WINDOW_MODAL);
		addCloseKeyShortcut(stage::close);
	}

	@Override
	public void init() {
		pluginList.setItems(filteredData);
		pluginList.getSelectionModel().selectedItemProperty().addListener(this);

		searchField.textProperty().addListener((observable, oldValue, newValue) -> {
			if(newValue == null || newValue.length() == 0) {
				filteredData.setPredicate(s -> true);
			}
			else {
				filteredData.setPredicate(p -> p.getDisplayName().contains(newValue));
			}
		});
	}

	@Override
	public void initStage(Stage stage) {
		stage.getIcons().add(PlayPadPlugin.getInstance().getIcon());

		stage.setMinWidth(600);
		stage.setMinHeight(400);
		stage.setTitle(Localization.getString(Strings.UI_Dialog_Plugins_Title));

		if (Profile.currentProfile() != null) {
			PlayPadPlugin.styleable().applyStyle(stage);
		} else {
			// Add Stylesheet manuel
			stage.getScene().getStylesheets().add("style/style.css");
		}
	}

	@Override
	public void changed(ObservableValue<? extends ModernPlugin> observable, ModernPlugin oldValue, ModernPlugin newValue) {
		if (newValue != null) {
			pluginInstallButton.setText(ModernPluginManager.getInstance().isActive(newValue) ?
					Localization.getString("plugins.button.uninstall") :
					Localization.getString("plugins.button.install"));
			pluginHeadlineLabel.setText(newValue.getDisplayName());
			pluginInfoLabel.setText(newValue.getDescription());
			pluginVersionLabel.setText(newValue.getVersion());
		} else {
			pluginHeadlineLabel.setText("");
			pluginInfoLabel.setText("");
			pluginVersionLabel.setText("");
		}
	}

	@FXML
	private void pluginInstallHandler(ActionEvent event) {
		ModernPlugin plugin = pluginList.getSelectionModel().getSelectedItem();
		if (plugin != null) {
			if (!ModernPluginManager.getInstance().isActive(plugin)) {
				try {
					GlobalSettings settings = PlayPadPlugin.getInstance().getGlobalSettings();
					PlayPadPlugin.getServerHandler().getServer().loadPlugin(plugin, settings.getUpdateChannel());
					ModernPluginManager.getInstance().loadPlugin(plugin);
				} catch (IOException e) {
					Logger.error(e);
					Alerts.getInstance()
							.createAlert(Alert.AlertType.ERROR,
									Localization.getString(Strings.Error_Plugins_Header),
									Localization.getString(Strings.Error_Plugins_Install, e.getMessage()),
									getContainingWindow()
							).showAndWait();
				}
			} else {
				ModernPluginManager.getInstance().unloadPlugin(plugin);
			}
		}
	}
}
