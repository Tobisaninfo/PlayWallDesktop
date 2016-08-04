package de.tobias.playpad.viewcontroller.main;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.ResourceBundle;

import de.tobias.playpad.AppUserInfoStrings;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ProjectReference;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.view.main.MainLayoutConnect;
import de.tobias.playpad.viewcontroller.SettingsTabViewController;
import de.tobias.playpad.viewcontroller.dialog.ImportDialog;
import de.tobias.playpad.viewcontroller.dialog.NewProjectDialog;
import de.tobias.playpad.viewcontroller.dialog.PluginViewController;
import de.tobias.playpad.viewcontroller.dialog.PrintDialog;
import de.tobias.playpad.viewcontroller.dialog.ProfileViewController;
import de.tobias.playpad.viewcontroller.dialog.ProjectManagerDialog;
import de.tobias.playpad.viewcontroller.option.SettingsViewController;
import de.tobias.playpad.viewcontroller.pad.PadDragListener;
import de.tobias.utils.application.ApplicationInfo;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.ui.Alertable;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Worker;
import de.tobias.utils.util.net.FileUpload;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public abstract class BasicMenuToolbarViewController extends MenuToolbarViewController implements EventHandler<ActionEvent> {

	// Menu
	@FXML protected Label volumeUpLabel;
	@FXML protected HBox iconHbox;
	@FXML protected MenuItem errorMenu;
	@FXML protected HBox pageHBox;
	@FXML protected MenuItem saveMenuItem;
	@FXML protected HBox toolbarHBox;
	@FXML protected CheckMenuItem fullScreenMenuItem;
	@FXML protected MenuItem settingsMenuItem;
	@FXML protected MenuBar menuBar;
	@FXML protected ToolBar toolbar;
	@FXML protected Menu recentOpenMenu;
	@FXML protected Slider volumeSlider;
	@FXML protected Menu extensionMenu;
	@FXML protected MenuItem profileMenu;
	@FXML protected Label volumeDownLabel;
	@FXML protected CheckMenuItem dndModeMenuItem;
	@FXML protected CheckMenuItem alwaysOnTopItem;
	@FXML protected Menu layoutMenu;

	// window references
	private IMainViewController mainViewController;
	private SettingsViewController settingsViewController;

	public BasicMenuToolbarViewController(String name, String path, ResourceBundle localization, IMainViewController mainViewController) {
		super(name, path, localization);
		this.mainViewController = mainViewController;
	}

	@Override
	public void init() {
		volumeDownLabel.setGraphic(new FontIcon("volume-item", FontAwesomeType.VOLUME_DOWN));
		volumeUpLabel.setGraphic(new FontIcon("volume-item", FontAwesomeType.VOLUME_UP));

		volumeSlider.setOnScroll(ev ->
		{
			volumeSlider.setValue(volumeSlider.getValue() - ev.getDeltaY() * 0.001);
			volumeSlider.setValue(volumeSlider.getValue() + ev.getDeltaX() * 0.001);
		});

		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();
		Registry<MainLayoutConnect> mainLayouts = PlayPadPlugin.getRegistryCollection().getMainLayouts();
		ToggleGroup group = new ToggleGroup();

		int index = 1; // Für Tastenkombination
		for (String layoutType : mainLayouts.getTypes()) {
			try {
				MainLayoutConnect connect = mainLayouts.getComponent(layoutType);

				RadioMenuItem item = new RadioMenuItem(connect.name());
				group.getToggles().add(item);
				item.setOnAction((e) ->
				{
					mainViewController.setMainLayout(connect);
					Profile.currentProfile().getProfileSettings().setMainLayoutType(connect.getType());
				});

				// Key Combi
				if (index < 10) {
					item.setAccelerator(KeyCombination.keyCombination("Shortcut+" + index));
				}

				if (connect.getType().equals(profileSettings.getMainLayoutType())) {
					item.setSelected(true);
				}

				layoutMenu.getItems().add(item);
			} catch (NoSuchComponentException e) {
				e.printStackTrace();
			}
			index++;
		}
	}

	@Override
	public void deinit() {}

	// Basic Event Handler
	@FXML
	void newDocumentHandler(ActionEvent event) {
		doAction(() ->
		{
			NewProjectDialog dialog = new NewProjectDialog(mainViewController.getStage());
			dialog.getStage().showAndWait();

			Project project = dialog.getProject();
			if (project != null) {
				PlayPadMain.getProgramInstance().openProject(project);
			}
		});
	}

	@FXML
	void openDocumentHandler(ActionEvent event) {
		doAction(() ->
		{
			Stage stage = mainViewController.getStage();
			Project currentProject = PlayPadMain.getProgramInstance().getCurrentProject();

			ProjectManagerDialog view = new ProjectManagerDialog(stage, currentProject);
			Optional<ProjectReference> result = view.showAndWait();

			if (result.isPresent()) {
				ProjectReference ref = result.get();

				try {
					Project project = Project.load(result.get(), true, ImportDialog.getInstance(stage));
					PlayPadMain.getProgramInstance().openProject(project);

					createRecentDocumentMenuItems();
				} catch (ProfileNotFoundException e) {
					e.printStackTrace();

					// Error Message
					String errorMessage = Localization.getString(Strings.Error_Profile_NotFound, ref.getProfileReference(),
							e.getLocalizedMessage());
					mainViewController.showError(errorMessage);

					// Neues Profile wählen
					Profile profile = ImportDialog.getInstance(stage).getUnkownProfile();
					ref.setProfileReference(profile.getRef());
				} catch (ProjectNotFoundException e) {
					e.printStackTrace();
					mainViewController.showError(Localization.getString(Strings.Error_Project_NotFound, ref, e.getLocalizedMessage()));
				} catch (Exception e) {
					e.printStackTrace();
					mainViewController.showError(Localization.getString(Strings.Error_Project_Open, ref, e.getLocalizedMessage()));
				}
			}
		});
	}

	@FXML
	void saveMenuHandler(ActionEvent event) {
		Project currentProject = PlayPadMain.getProgramInstance().getCurrentProject();

		try {
			currentProject.save();
			mainViewController.notify(Localization.getString(Strings.Standard_File_Save), PlayPadMain.displayTimeMillis);
		} catch (IOException e) {
			mainViewController.showError(Localization.getString(Strings.Error_Project_Save));
			e.printStackTrace();
		}
	}

	@FXML
	void profileMenuHandler(ActionEvent event) {
		doAction(() ->
		{
			Project currentProject = PlayPadMain.getProgramInstance().getCurrentProject();

			ProfileViewController controller = new ProfileViewController(mainViewController.getStage(), currentProject);
			controller.getStage().showAndWait();
			mainViewController.setTitle();
		});
	}

	@FXML
	void printMenuHandler(ActionEvent event) {
		Project currentProject = PlayPadMain.getProgramInstance().getCurrentProject();
		PrintDialog dialog = new PrintDialog(currentProject, mainViewController.getStage());
		dialog.getStage().show();
	}

	@FXML
	void dndModeHandler(ActionEvent event) {
		if (dndModeMenuItem.isSelected()) {
			ProfileSettings settings = Profile.currentProfile().getProfileSettings();
			Project currentProject = PlayPadMain.getProgramInstance().getCurrentProject();

			if (settings.isLiveMode() && settings.isLiveModeDrag() && currentProject.getPlayedPlayers() > 0) {
				mainViewController.showLiveInfo();
			} else {
				PadDragListener.setDndMode(true);
				for (IPadViewV2 view : mainViewController.getPadViews()) {
					view.enableDragAndDropDesignMode(true);
				}
			}
		} else {
			PadDragListener.setDndMode(false);
			for (IPadViewV2 view : mainViewController.getPadViews()) {
				view.enableDragAndDropDesignMode(false);
			}
		}

	}

	@FXML
	void errorMenuHandler(ActionEvent event) {
		// TODO Implement
	}

	@FXML
	void pluginMenuItemHandler(ActionEvent event) {
		doAction(() ->
		{
			PluginViewController controller = new PluginViewController(mainViewController.getStage());
			controller.getStage().showAndWait();
		});
	}

	@FXML
	void settingsHandler(ActionEvent event) {
		Midi midi = Midi.getInstance();
		Project currentProject = PlayPadMain.getProgramInstance().getCurrentProject();

		ProfileSettings settings = Profile.currentProfile().getProfileSettings();

		if (settings.isLiveMode() && settings.isLiveModeSettings() && currentProject.getPlayedPlayers() > 0) {
			mainViewController.showLiveInfo();
			return;
		}

		if (settingsViewController == null) {
			Stage mainStage = mainViewController.getStage();

			settingsViewController = new SettingsViewController(midi, mainViewController.getScreen(), mainStage, currentProject, () ->
			{
				midi.setListener(mainViewController.getMidiHandler());

				boolean change = false;
				for (SettingsTabViewController controller : settingsViewController.getTabs()) {
					if (controller.needReload()) {
						change = true;
						controller.reload(Profile.currentProfile(), currentProject, mainViewController);
					}
				}

				if (change) {
					PlayPadMain.getProgramInstance().getSettingsListener().forEach(l -> l.onChange(Profile.currentProfile()));
				}

				settingsViewController = null;
				mainStage.toFront();
			});

			settingsViewController.getStage().show();
		} else if (settingsViewController.getStage().isShowing()) {
			settingsViewController.getStage().toFront();
		}

	}

	@FXML
	void alwaysOnTopItemHandler(ActionEvent event) {
		boolean selected = alwaysOnTopItem.isSelected();

		mainViewController.getStage().setAlwaysOnTop(selected);
		Profile.currentProfile().getProfileSettings().setWindowAlwaysOnTop(selected);
	}

	@FXML
	void fullScreenMenuItemHandler(ActionEvent event) {
		mainViewController.getStage().setFullScreen(fullScreenMenuItem.isSelected());
	}

	@FXML
	void aboutMenuHandler(ActionEvent event) {
		ApplicationInfo info = ApplicationUtils.getApplication().getInfo();
		String message = Localization.getString(Strings.UI_Dialog_Info_Content, info.getVersion(), info.getBuild(), info.getAuthor());
		if (mainViewController instanceof Alertable) {
			((Alertable) mainViewController).showInfoMessage(message, Localization.getString(Strings.UI_Dialog_Info_Header, info.getName()),
					PlayPadMain.stageIcon.orElse(null));
		}
	}

	@FXML
	void visiteWebsiteMenuHandler(ActionEvent event) {
		String website = ApplicationUtils.getApplication().getInfo().getUserInfo().getString(AppUserInfoStrings.WEBSITE);
		try {
			Desktop.getDesktop().browse(new URI(website));
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@FXML
	void sendErrorMenuItem(ActionEvent event) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.initOwner(mainViewController.getStage());
		alert.initModality(Modality.WINDOW_MODAL);
		Stage dialog = (Stage) alert.getDialogPane().getScene().getWindow();
		PlayPadMain.stageIcon.ifPresent(dialog.getIcons()::add);
		alert.setContentText(Localization.getString(Strings.UI_Dialog_Feedback_Content));
		alert.show();

		Worker.runLater(() ->
		{
			try {
				String response = FileUpload.fileUpload(
						ApplicationUtils.getApplication().getInfo().getUserInfo().getString(AppUserInfoStrings.ERROR_URL),
						ApplicationUtils.getApplication().getPath(PathType.LOG, "err.log").toFile());
				Platform.runLater(() -> alert.setContentText(response));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private final int LAST_DOCUMENT_LIMIT = 3;

	public void createRecentDocumentMenuItems() {
		recentOpenMenu.getItems().clear();

		Project currentProject = PlayPadMain.getProgramInstance().getCurrentProject();
		String project = currentProject.getRef().getName();

		ProjectReference.getProjectsSorted().stream().filter(item -> !item.getName().equals(project)).limit(LAST_DOCUMENT_LIMIT).forEach(item ->
		{
			MenuItem menuItem = new MenuItem(item.toString());
			menuItem.setUserData(item);
			menuItem.setOnAction(this);
			recentOpenMenu.getItems().add(menuItem);
		});
	}

	// Utils
	protected void doAction(Runnable run) {
		Project project = PlayPadMain.getProgramInstance().getCurrentProject();
		if (project.getPlayedPlayers() > 0 && Profile.currentProfile().getProfileSettings().isLiveMode()) {
			mainViewController.showLiveInfo();
		} else {
			run.run();
		}
	}

}
