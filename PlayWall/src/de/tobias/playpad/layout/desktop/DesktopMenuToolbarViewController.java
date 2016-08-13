package de.tobias.playpad.layout.desktop;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.controlsfx.control.textfield.TextFields;

import de.tobias.playpad.AppUserInfoStrings;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.v2.ProjectV2;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.settings.keys.KeyCollection;
import de.tobias.playpad.view.HelpMenuItem;
import de.tobias.playpad.view.main.MainLayoutConnect;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.dialog.ImportDialog;
import de.tobias.playpad.viewcontroller.dialog.NewProjectDialog;
import de.tobias.playpad.viewcontroller.dialog.PluginViewController;
import de.tobias.playpad.viewcontroller.dialog.PrintDialog;
import de.tobias.playpad.viewcontroller.dialog.ProfileViewController;
import de.tobias.playpad.viewcontroller.dialog.ProjectManagerDialog;
import de.tobias.playpad.viewcontroller.main.BasicMenuToolbarViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.GlobalSettingsTabViewController;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import de.tobias.playpad.viewcontroller.option.ProjectSettingsTabViewController;
import de.tobias.playpad.viewcontroller.option.global.GlobalSettingsViewController;
import de.tobias.playpad.viewcontroller.option.profile.ProfileSettingsViewController;
import de.tobias.playpad.viewcontroller.option.project.ProjectSettingsViewController;
import de.tobias.playpad.viewcontroller.pad.PadDragListener;
import de.tobias.utils.application.ApplicationInfo;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.ui.Alertable;
import de.tobias.utils.ui.scene.NotificationPane;
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.Worker;
import de.tobias.utils.util.net.FileUpload;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DesktopMenuToolbarViewController extends BasicMenuToolbarViewController implements EventHandler<ActionEvent> {

	// meuBar
	@FXML protected MenuBar menuBar;

	@FXML protected MenuItem newProjectMenuItem;
	@FXML protected MenuItem openProjectMenuItem;
	@FXML protected MenuItem saveProjectMenuItem;
	@FXML protected MenuItem profileMenu;
	@FXML protected MenuItem printProjectMenuItem;

	@FXML protected CheckMenuItem dndModeMenuItem;
	@FXML protected MenuItem errorMenu;
	@FXML protected MenuItem pluginMenu;

	@FXML protected MenuItem projectSettingsMenuItem;
	@FXML protected MenuItem profileSettingsMenuItem;
	@FXML protected MenuItem globalSettingsMenuItem;

	@FXML protected CheckMenuItem fullScreenMenuItem;
	@FXML protected CheckMenuItem alwaysOnTopItem;
	@FXML protected MenuItem searchPadMenuItem;

	@FXML protected Menu layoutMenu;

	@FXML protected Menu extensionMenu;
	@FXML protected Menu infoMenu;
	@FXML protected Menu helpMenu;

	@FXML protected Label liveLabel;

	private IMainViewController mainViewController;

	private ProjectSettingsViewController projectSettingsViewController;
	private ProfileSettingsViewController profileSettingsViewController;
	private GlobalSettingsViewController globalSettingsViewController;

	public DesktopMenuToolbarViewController(IMainViewController controller) {
		super("header", "de/tobias/playpad/assets/view/main/desktop/", PlayPadMain.getUiResourceBundle(), controller);
		this.mainViewController = controller;

		initLayoutMenu();
	}

	@Override
	public void init() {
		super.init();
		toolbarHBox.prefWidthProperty().bind(toolbar.widthProperty().subtract(25));
		toolbarHBox.prefHeightProperty().bind(toolbar.minHeightProperty());

		showLiveInfo(false);

		helpMenu.getItems().add(new HelpMenuItem(helpMenu));
	}

	private void initLayoutMenu() {
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();
		Registry<MainLayoutConnect> mainLayouts = PlayPadPlugin.getRegistryCollection().getMainLayouts();
		ToggleGroup group = new ToggleGroup();

		int index = 1; // Für Tastenkombination
		for (String layoutType : mainLayouts.getTypes()) {
			try {
				MainLayoutConnect connect = mainLayouts.getComponent(layoutType);

				RadioMenuItem item = new RadioMenuItem(connect.name());
				item.setUserData(connect);
				group.getToggles().add(item);

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

		group.selectedToggleProperty().addListener((a, b, c) ->
		{
			if (c instanceof RadioMenuItem) {
				RadioMenuItem menuItem = (RadioMenuItem) c;
				if (menuItem.getUserData() instanceof MainLayoutConnect) {
					MainLayoutConnect connect = (MainLayoutConnect) menuItem.getUserData();
					mainViewController.setMainLayout(connect);
					Profile.currentProfile().getProfileSettings().setMainLayoutType(connect.getType());
				}
			}
		});
	}

	@Override
	public void setOpenProject(ProjectV2 project) {
		super.setOpenProject(project);
		if (project != null)
			createRecentDocumentMenuItems();
	}

	@Override
	public void initPageButtons() {
		pageHBox.getChildren().clear();

		if (openProject == null) {
			return;
		}

		for (int i = 0; i < openProject.getSettings().getPageCount(); i++) {
			Button button = new Button(Localization.getString(Strings.UI_Window_Main_PageButton, (i + 1)));
			button.setUserData(i);
			button.setFocusTraversable(false);
			button.setOnAction(this);
			pageHBox.getChildren().add(button);
		}
	}

	@Override
	public void loadKeybinding(KeyCollection keys) {
		setKeyBindingForMenu(newProjectMenuItem, keys.getKey("new_proj"));
		setKeyBindingForMenu(openProjectMenuItem, keys.getKey("open_proj"));
		setKeyBindingForMenu(saveProjectMenuItem, keys.getKey("save_proj"));
		setKeyBindingForMenu(printProjectMenuItem, keys.getKey("print_proj"));

		setKeyBindingForMenu(dndModeMenuItem, keys.getKey("dnd"));
		setKeyBindingForMenu(errorMenu, keys.getKey("errors"));
		setKeyBindingForMenu(pluginMenu, keys.getKey("plugins"));
		setKeyBindingForMenu(projectSettingsMenuItem, keys.getKey("project_settings"));
		setKeyBindingForMenu(profileSettingsMenuItem, keys.getKey("profile_settings"));
		setKeyBindingForMenu(globalSettingsMenuItem, keys.getKey("global_settings"));

		setKeyBindingForMenu(fullScreenMenuItem, keys.getKey("window_fullscreen"));
		setKeyBindingForMenu(alwaysOnTopItem, keys.getKey("window_top"));
		setKeyBindingForMenu(searchPadMenuItem, keys.getKey("search_pad"));

		newProjectMenuItem.setDisable(false);
		openProjectMenuItem.setDisable(false);
		saveProjectMenuItem.setDisable(false);
		printProjectMenuItem.setDisable(false);

		dndModeMenuItem.setDisable(false);
		errorMenu.setDisable(false);
		pluginMenu.setDisable(false);
		projectSettingsMenuItem.setDisable(false);
		profileSettingsMenuItem.setDisable(false);
		globalSettingsMenuItem.setDisable(false);

		fullScreenMenuItem.setDisable(false);
		alwaysOnTopItem.setDisable(false);
		searchPadMenuItem.setDisable(false);
	}

	@Override
	public void setLocked(boolean looked) {
		dndModeMenuItem.setDisable(looked);
	}

	@Override
	public void setAlwaysOnTopActive(boolean alwaysOnTopActive) {
		alwaysOnTopItem.setSelected(alwaysOnTopActive);
	}

	@Override
	public void setFullScreenActive(boolean fullScreenActive) {
		fullScreenMenuItem.setSelected(fullScreenActive);
	}

	@Override
	public void addToolbarItem(Node node) {
		iconHbox.getChildren().add(node);
	}

	@Override
	public void removeToolbarItem(Node node) {
		iconHbox.getChildren().remove(node);
	}

	@Override
	public void addMenuItem(MenuItem item, MenuType type) {
		if (type == MenuType.EXTENSION) {
			extensionMenu.getItems().add(item);
		} else if (type == MenuType.SETTINGS) {
			// TODO Implement
		}
	}

	@Override
	public void removeMenuItem(MenuItem item) {
		if (extensionMenu.getItems().contains(item))
			extensionMenu.getItems().remove(item);

		// TODO Implement
	}

	@Override
	public boolean isAlwaysOnTopActive() {
		return alwaysOnTopItem.isSelected();
	}

	@Override
	public boolean isFullscreenActive() {
		return fullScreenMenuItem.isSelected();
	}

	@Override
	public void deinit() {
		newProjectMenuItem.setDisable(true);
		openProjectMenuItem.setDisable(true);
		saveProjectMenuItem.setDisable(true);
		printProjectMenuItem.setDisable(true);

		dndModeMenuItem.setDisable(true);
		errorMenu.setDisable(true);
		pluginMenu.setDisable(true);
		projectSettingsMenuItem.setDisable(true);
		profileSettingsMenuItem.setDisable(true);
		globalSettingsMenuItem.setDisable(true);

		fullScreenMenuItem.setDisable(true);
		alwaysOnTopItem.setDisable(true);
		searchPadMenuItem.setDisable(true);

		// Disable Drag Mode wenn aktiv und diese Toolbar deaktiviert wird.
		if (dndModeMenuItem.isSelected()) {
			PadDragListener.setDndMode(false);
			for (IPadViewV2 view : mainViewController.getPadViews()) {
				view.enableDragAndDropDesignMode(false);
			}
		}
	}

	@Override
	public void showLiveInfo(boolean show) {
		liveLabel.setVisible(show);
	}

	@Override
	public Slider getVolumeSlider() {
		return volumeSlider;
	}

	private int currentPage = 0;

	@Override
	public void highlightPageButton(int index) {
		if (index >= 0) {
			if (pageHBox.getChildren().size() > currentPage) {
				Node removeNode = pageHBox.getChildren().get(currentPage);
				removeNode.getStyleClass().remove(CURRENT_PAGE_BUTTON);
			}

			if (pageHBox.getChildren().size() > index) {
				Node newNode = pageHBox.getChildren().get(index);
				newNode.getStyleClass().add(CURRENT_PAGE_BUTTON);
				currentPage = index;
			}
		}
	}

	// EventHandler
	@FXML
	void newDocumentHandler(ActionEvent event) {
		doAction(() ->
		{
			NewProjectDialog dialog = new NewProjectDialog(mainViewController.getStage());
			dialog.getStage().showAndWait();

			ProjectV2 project = dialog.getProject();
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

			ProjectManagerDialog view = new ProjectManagerDialog(stage, openProject);
			Optional<ProjectReference> result = view.showAndWait();

			if (result.isPresent()) {
				ProjectReference ref = result.get();

				try {
					ProjectV2 project = ProjectV2.load(result.get(), true, ImportDialog.getInstance(stage));
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
		try {
			openProject.save();
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
			ProfileViewController controller = new ProfileViewController(mainViewController.getStage(), openProject);
			controller.getStage().showAndWait();
			mainViewController.updateWindowTitle();
		});
	}

	@FXML
	void printMenuHandler(ActionEvent event) {
		PrintDialog dialog = new PrintDialog(openProject, mainViewController.getStage());
		dialog.getStage().show();
	}

	@FXML
	void dndModeHandler(ActionEvent event) {
		if (dndModeMenuItem.isSelected()) {
			ProfileSettings settings = Profile.currentProfile().getProfileSettings();
			if (settings.isLiveMode() && settings.isLiveModeDrag() && openProject.getPlayedPlayers() > 0) {
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
	void projectSettingsHandler(ActionEvent event) {
		Midi midi = Midi.getInstance();
		if (projectSettingsViewController == null) {
			Stage mainStage = mainViewController.getStage();

			Runnable onFinish = () ->
			{
				midi.setListener(mainViewController.getMidiHandler());

				for (ProjectSettingsTabViewController controller : projectSettingsViewController.getTabs()) {
					if (controller.needReload()) {
						controller.reload(openProject.getSettings(), openProject, mainViewController);
					}
				}

				profileSettingsViewController = null;
				mainStage.toFront();
			};

			projectSettingsViewController = new ProjectSettingsViewController(mainViewController.getScreen(), mainStage, openProject, onFinish);

			projectSettingsViewController.getStage().show();
		} else if (projectSettingsViewController.getStage().isShowing()) {
			projectSettingsViewController.getStage().toFront();
		}
	}

	@FXML
	void profileSettingsHandler(ActionEvent event) {
		Midi midi = Midi.getInstance();
		ProfileSettings settings = Profile.currentProfile().getProfileSettings();

		if (settings.isLiveMode() && settings.isLiveModeSettings() && openProject.getPlayedPlayers() > 0) {
			mainViewController.showLiveInfo();
			return;
		}

		if (profileSettingsViewController == null) {
			Stage mainStage = mainViewController.getStage();

			Runnable onFinish = () ->
			{
				midi.setListener(mainViewController.getMidiHandler());

				boolean change = false;
				for (ProfileSettingsTabViewController controller : profileSettingsViewController.getTabs()) {
					if (controller.needReload()) {
						change = true;
						controller.reload(Profile.currentProfile(), openProject, mainViewController);
					}
				}

				if (change) {
					PlayPadMain.getProgramInstance().getSettingsListener().forEach(l -> l.onChange(Profile.currentProfile()));
				}

				profileSettingsViewController = null;
				mainStage.toFront();
			};

			profileSettingsViewController = new ProfileSettingsViewController(midi, mainViewController.getScreen(), mainStage, openProject,
					onFinish);

			profileSettingsViewController.getStage().show();
		} else if (profileSettingsViewController.getStage().isShowing()) {
			profileSettingsViewController.getStage().toFront();
		}
	}

	@FXML
	void globalSettingsHandler(ActionEvent event) {
		if (globalSettingsViewController == null) {

			Stage mainStage = mainViewController.getStage();
			Runnable onFinish = () ->
			{
				GlobalSettings globalSettings = PlayPadPlugin.getImplementation().getGlobalSettings();

				for (GlobalSettingsTabViewController controller : globalSettingsViewController.getTabs()) {
					if (controller.needReload()) {
						controller.reload(globalSettings, openProject, mainViewController);
					}
				}

				globalSettingsViewController = null;
				mainStage.toFront();
			};

			globalSettingsViewController = new GlobalSettingsViewController(mainStage, onFinish);
			globalSettingsViewController.getStage().show();
		} else {
			globalSettingsViewController.getStage().toFront();
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
	void searchPadHandler(ActionEvent event) {
		TextField field = TextFields.createClearableTextField();
		field.setPromptText("Suche"); // TODO i18n

		Button button = new Button("Suchen"); // TODO i18n
		button.setOnAction(new DesktopSearchController(field, this));

		HBox box = new HBox(14, field, button);
		box.setAlignment(Pos.CENTER_LEFT);

		NotificationPane pane = mainViewController.getNotificationPane();
		pane.show("", box);

		// Auto Complete
		Set<String> names = openProject.getPads().stream().filter(p -> p.getStatus() != PadStatus.EMPTY).map(Pad::getName)
				.collect(Collectors.toSet());
		TextFields.bindAutoCompletion(field, names);
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

		String project = openProject.getProjectReference().getName();

		ProjectReference.getProjectsSorted().stream().filter(item -> !item.getName().equals(project)).limit(LAST_DOCUMENT_LIMIT).forEach(item ->
		{
			MenuItem menuItem = new MenuItem(item.toString());
			menuItem.setUserData(item);
			menuItem.setOnAction(this);
			recentOpenMenu.getItems().add(menuItem);
		});
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() instanceof Button) {
			Button button = (Button) event.getSource();
			int page = (int) button.getUserData();
			mainViewController.showPage(page);
		} else if (event.getSource() instanceof MenuItem) {
			// Zuletzt verwendete Projects
			doAction(() ->
			{
				// TODO Rewrite mit openProject von BasicMenuToolbarViewController
				MenuItem item = (MenuItem) event.getSource();
				ProjectReference ref = (ProjectReference) item.getUserData();
				try {
					// Speichern das alte Project in mvc.setProject(Project)
					ProjectV2 project = ProjectV2.load(ref, true, ImportDialog.getInstance(mainViewController.getStage()));
					PlayPadMain.getProgramInstance().openProject(project);
				} catch (ProfileNotFoundException e) {
					e.printStackTrace();
					mainViewController.showError(
							Localization.getString(Strings.Error_Profile_NotFound, ref.getProfileReference(), e.getLocalizedMessage()));

					// Neues Profile wählen
					Profile profile = ImportDialog.getInstance(mainViewController.getStage()).getUnkownProfile();
					ref.setProfileReference(profile.getRef());
				} catch (ProjectNotFoundException e) {
					e.printStackTrace();
					mainViewController.showError(Localization.getString(Strings.Error_Project_NotFound, ref, e.getLocalizedMessage()));
				} catch (Exception e) {
					e.printStackTrace();
					mainViewController.showError(Localization.getString(Strings.Error_Project_Open, ref, e.getLocalizedMessage()));
				}
			});
		}
	}
}
