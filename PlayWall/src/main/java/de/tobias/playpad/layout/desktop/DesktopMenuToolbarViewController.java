package de.tobias.playpad.layout.desktop;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.ui.NVCStage;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.scene.NotificationPane;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.AppUserInfoStrings;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.design.ColorModeHandler;
import de.tobias.playpad.design.modern.ModernGlobalDesignHandler;
import de.tobias.playpad.layout.desktop.listener.DesktopSearchController;
import de.tobias.playpad.layout.desktop.listener.PadRemoveMouseListener;
import de.tobias.playpad.layout.desktop.listener.PageButtonDragHandler;
import de.tobias.playpad.pad.view.IPadView;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileNotFoundException;
import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.playpad.profile.ref.ProfileReference;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ProjectReader.ProjectReaderDelegate.ProfileAbortException;
import de.tobias.playpad.project.ProjectSettings;
import de.tobias.playpad.project.ProjectSettingsValidator;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.project.ref.ProjectReference;
import de.tobias.playpad.project.ref.ProjectReferenceManager;
import de.tobias.playpad.registry.Registry;
import de.tobias.playpad.server.Session;
import de.tobias.playpad.server.SessionNotExistsException;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.settings.keys.KeyCollection;
import de.tobias.playpad.view.main.MainLayoutFactory;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.AuthViewController;
import de.tobias.playpad.viewcontroller.dialog.AboutDialog;
import de.tobias.playpad.viewcontroller.dialog.ModernPluginViewController;
import de.tobias.playpad.viewcontroller.dialog.PrintDialog;
import de.tobias.playpad.viewcontroller.dialog.ProfileViewController;
import de.tobias.playpad.viewcontroller.dialog.project.ProjectManagerDialog;
import de.tobias.playpad.viewcontroller.dialog.project.ProjectNewDialog;
import de.tobias.playpad.viewcontroller.dialog.project.ProjectReaderDelegateImpl;
import de.tobias.playpad.viewcontroller.main.BasicMenuToolbarViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.playpad.viewcontroller.option.global.GlobalSettingsViewController;
import de.tobias.playpad.viewcontroller.option.profile.ProfileSettingsViewController;
import de.tobias.playpad.viewcontroller.option.project.ProjectSettingsViewController;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.control.textfield.TextFields;
import org.dom4j.DocumentException;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class DesktopMenuToolbarViewController extends BasicMenuToolbarViewController
		implements EventHandler<ActionEvent>, ChangeListener<DesktopEditMode> {

	private static final String LAYOUT_MENU_ITEM_IDENTIFIER = "layout-menu-item";

	@FXML
	private MenuBar menuBar;

	@FXML
	private MenuItem newProjectMenuItem;
	@FXML
	private MenuItem openProjectMenuItem;
	@FXML
	private MenuItem saveProjectMenuItem;
	@FXML
	private MenuItem profileMenu;
	@FXML
	private MenuItem printProjectMenuItem;
	@FXML
	private MenuItem logoutMenuItem;

	@FXML
	private MenuItem playMenu;
	@FXML
	private MenuItem dragMenu;
	@FXML
	private MenuItem pageMenu;
	@FXML
	private MenuItem colorMenu;

	@FXML
	private MenuItem notFoundMenu;
	@FXML
	private MenuItem pluginMenu;

	@FXML
	private MenuItem projectSettingsMenuItem;
	@FXML
	private MenuItem profileSettingsMenuItem;
	@FXML
	private MenuItem globalSettingsMenuItem;

	@FXML
	private CheckMenuItem fullScreenMenuItem;
	@FXML
	private CheckMenuItem alwaysOnTopItem;
	@FXML
	private MenuItem searchPadMenuItem;

	@FXML
	private Menu layoutMenu;

	@FXML
	private Menu extensionMenu;
	@FXML
	protected Menu infoMenu;

	@FXML
	private Label liveLabel;

	private SegmentedButton editButtons;
	private ToggleButton playButton;
	private ToggleButton dragButton;
	private ToggleButton pageButton;
	private ToggleButton colorButton;
	private Button addPageButton;

	private final IMainViewController mainViewController;

	private transient ProjectSettingsViewController projectSettingsViewController;
	private transient ProfileSettingsViewController profileSettingsViewController;
	private transient GlobalSettingsViewController globalSettingsViewController;

	private transient DesktopColorPickerView colorPickerView;
	private transient PadRemoveMouseListener padRemoveMouseListener;

	private final DesktopMainLayoutFactory connect;

	DesktopMenuToolbarViewController(IMainViewController controller, DesktopMainLayoutFactory connect) {
		super("Header", "view/main/desktop", Localization.getBundle());
		this.mainViewController = controller;
		this.connect = connect;
		this.connect.editModeProperty().addListener(this);

		changed(connect.editModeProperty(), null, connect.getEditMode());

		initLayoutMenu();
	}

	@Override
	public void init() {
		super.init();

		// Hide Extension menu then no items are in there
		extensionMenu.visibleProperty().bind(Bindings.size(extensionMenu.getItems()).greaterThan(0));

		// Edit Mode Buttons
		editButtons = new SegmentedButton();
		playButton = new ToggleButton("", new FontIcon(FontAwesomeType.PLAY));
		playButton.setTooltip(new Tooltip(Localization.getString(Strings.TOOLTIP_PLAY_BUTTON)));
		playButton.setFocusTraversable(false);
		dragButton = new ToggleButton("", new FontIcon(FontAwesomeType.ARROWS));
		dragButton.setTooltip(new Tooltip(Localization.getString(Strings.TOOLTIP_DRAG_BUTTON)));
		dragButton.setFocusTraversable(false);
		pageButton = new ToggleButton("", new FontIcon(FontAwesomeType.FILES_ALT));
		pageButton.setTooltip(new Tooltip(Localization.getString(Strings.TOOLTIP_PAGE_BUTTON)));
		pageButton.setFocusTraversable(false);
		colorButton = new ToggleButton("", new FontIcon(FontAwesomeType.PENCIL));
		colorButton.setTooltip(new Tooltip(Localization.getString(Strings.TOOLTIP_COLOR_BUTTON)));
		colorButton.setFocusTraversable(false);
		editButtons.getButtons().addAll(playButton, dragButton, pageButton, colorButton);
		editButtons.getToggleGroup().selectedToggleProperty().addListener((a, b, c) ->
		{
			if (c == playButton) {
				connect.setEditMode(DesktopEditMode.PLAY);
			} else if (c == dragButton) {
				connect.setEditMode(DesktopEditMode.DRAG);
			} else if (c == pageButton) {
				connect.setEditMode(DesktopEditMode.PAGE);
			} else if (c == colorButton) {
				connect.setEditMode(DesktopEditMode.COLOR);

				if (colorPickerView != null) {
					colorPickerView.show(colorButton);
				}
			} else if (c == null) {
				// select Old Button, if new selecting is empty
				editButtons.getToggleGroup().selectToggle(b);
			}
		});

		// Add Page Button for Drag Mode (Page Edit Mode)
		addPageButton = new Button("", new FontIcon(FontAwesomeType.PLUS));
		addPageButton.setFocusTraversable(false);
		addPageButton.setOnAction(e ->
		{
			if (openProject.addPage()) {
				// seite konnte hinzugefügt werden
				initPageButtons();
				highlightPageButton(mainViewController.getPage());
			} else {
				showErrorMessage(Localization.getString(Strings.ERROR_PROJECT_PAGE_COUNT, ProjectSettings.MAX_PAGES));
			}
		});

		iconHbox.getChildren().add(editButtons);

		volumeSlider.focusedProperty().addListener(i -> mainViewController.getParent().requestFocus());
	}

	// Desktop Edit Mode Change Listener --> Update Button
	@Override
	public void changed(ObservableValue<? extends DesktopEditMode> observable, DesktopEditMode oldValue, DesktopEditMode newValue) {

		// handle old mode
		if (oldValue == DesktopEditMode.DRAG) {
			for (IPadView view : mainViewController.getPadViews()) {
				view.enableDragAndDropDesignMode(false);
			}
			mainViewController.removeListenerForPads(padRemoveMouseListener, MouseEvent.MOUSE_CLICKED);
		} else if (oldValue == DesktopEditMode.PAGE) {
			highlightPageButton(currentSelectedPageButton);
			iconHbox.getChildren().remove(addPageButton);
		} else if (oldValue == DesktopEditMode.COLOR) {
			if (colorPickerView != null) {
				mainViewController.removeListenerForPads(colorPickerView, MouseEvent.MOUSE_CLICKED);
				colorPickerView.hide();
				colorPickerView = null;
			}
		}

		// handle new mode
		if (newValue == DesktopEditMode.PLAY) {
			playButton.setSelected(true);
		} else if (newValue == DesktopEditMode.DRAG) {
			// Wenn Live Mode on, dann zum alten Wert zurück
			GlobalSettings settings = PlayPadPlugin.getInstance().getGlobalSettings();
			if (settings.isLiveMode() && settings.isLiveModeDrag() && openProject.getActivePlayers() != 0) {
				connect.setEditMode(oldValue);
				return;
			}

			// Add Pad Remove Listener
			if (padRemoveMouseListener == null) {
				padRemoveMouseListener = new PadRemoveMouseListener();
			}
			mainViewController.addListenerForPads(padRemoveMouseListener, MouseEvent.MOUSE_CLICKED);

			// Drag and Drop Aktivieren
			dragButton.setSelected(true);
			for (IPadView view : mainViewController.getPadViews()) {
				view.enableDragAndDropDesignMode(true);
			}
		} else if (newValue == DesktopEditMode.PAGE) {
			pageButton.setSelected(true);
			iconHbox.getChildren().add(0, addPageButton);
			highlightPageButton(currentSelectedPageButton);
		} else if (newValue == DesktopEditMode.COLOR) {
			colorButton.setSelected(true);

			ModernGlobalDesignHandler designHandler = PlayPadMain.getProgramInstance().getModernDesign().global();
			if (designHandler instanceof ColorModeHandler) {
				if (colorPickerView == null) {
					colorPickerView = new DesktopColorPickerView((ColorModeHandler) designHandler);

					// Add Listener for Pads
					mainViewController.addListenerForPads(colorPickerView, MouseEvent.MOUSE_CLICKED);
				}
				colorPickerView.show(colorButton);
			}
		}

		mainViewController.getPadViews().forEach(i -> i.getViewController().updateButtonDisable());
	}

	@Override
	public void initLayoutMenu() {
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();
		Registry<MainLayoutFactory> mainLayouts = PlayPadPlugin.getRegistries().getMainLayouts();

		layoutMenu.getItems().removeIf(item -> LAYOUT_MENU_ITEM_IDENTIFIER.equals(item.getUserData()));

		int index = 1; // Für Tastenkombination
		for (MainLayoutFactory connect : mainLayouts.getComponents()) {
			if (connect.getType().equals(profileSettings.getMainLayoutType())) {
				continue;
			}

			MenuItem item = new MenuItem(connect.toString());
			item.setUserData(LAYOUT_MENU_ITEM_IDENTIFIER);

			item.setOnAction(e ->
			{
				mainViewController.setMainLayout(connect);
				Profile.currentProfile().getProfileSettings().setMainLayoutType(connect.getType());
			});

			// Key combination
			if (index < 10) {
				item.setAccelerator(KeyCombination.keyCombination("Shortcut+" + index));
			}

			layoutMenu.getItems().add(item);
			index++;
		}
	}

	@Override
	public void setOpenProject(Project project) {
		super.setOpenProject(project);

		liveLabel.visibleProperty().unbind();

		if (project != null) {
			createRecentDocumentMenuItems();
			liveLabel.visibleProperty().bind(project.activePlayerProperty().greaterThan(0));
		}
	}

	@Override
	public void initPageButtons() {
		currentSelectedPageButton = -1;
		pageHBox.getChildren().clear();

		if (openProject == null) {
			return;
		}

		for (int i = 0; i < openProject.getPages().size(); i++) {
			Page page = openProject.getPage(i);

			Button button = createPageButton(page, i);
			button.setOnDragOver(new PageButtonDragHandler(mainViewController, i));
			pageHBox.getChildren().add(button);
		}
	}

	@Override
	public void loadKeybinding(KeyCollection keys) {
		setKeyBindingForMenu(newProjectMenuItem, keys.getKey("new_proj"));
		setKeyBindingForMenu(openProjectMenuItem, keys.getKey("open_proj"));
		setKeyBindingForMenu(saveProjectMenuItem, keys.getKey("save_proj"));
		setKeyBindingForMenu(printProjectMenuItem, keys.getKey("print_proj"));

		setKeyBindingForMenu(playMenu, keys.getKey("play"));
		setKeyBindingForMenu(dragMenu, keys.getKey("drag"));
		setKeyBindingForMenu(pageMenu, keys.getKey("page"));
		setKeyBindingForMenu(colorMenu, keys.getKey("color"));

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

		playMenu.setDisable(false);
		dragMenu.setDisable(false);
		pageMenu.setDisable(false);
		colorMenu.setDisable(false);

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
		connect.setEditMode(DesktopEditMode.PLAY);
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
		}
	}

	@Override
	public void removeMenuItem(MenuItem item) {
		extensionMenu.getItems().remove(item);
	}

	@Override
	public void deinit() {
		newProjectMenuItem.setDisable(true);
		openProjectMenuItem.setDisable(true);
		saveProjectMenuItem.setDisable(true);
		printProjectMenuItem.setDisable(true);

		playMenu.setDisable(true);
		dragMenu.setDisable(true);
		pageMenu.setDisable(true);
		colorMenu.setDisable(true);

		pluginMenu.setDisable(true);
		projectSettingsMenuItem.setDisable(true);
		profileSettingsMenuItem.setDisable(true);
		globalSettingsMenuItem.setDisable(true);

		fullScreenMenuItem.setDisable(true);
		alwaysOnTopItem.setDisable(true);
		searchPadMenuItem.setDisable(true);

		connect.setEditMode(DesktopEditMode.PLAY);
	}

	@Override
	public Slider getVolumeSlider() {
		return volumeSlider;
	}

	private int currentSelectedPageButton = 0;

	@Override
	public void highlightPageButton(int index) {
		if (index >= 0) {
			if (pageHBox.getChildren().size() > currentSelectedPageButton && currentSelectedPageButton >= 0) {
				Node removeNode = pageHBox.getChildren().get(currentSelectedPageButton);
				removeNode.getStyleClass().remove(CURRENT_PAGE_BUTTON);

				if (removeNode instanceof Button) {
					((Button) removeNode).setGraphic(null);
				}
			}

			if (index < pageHBox.getChildren().size()) {
				Node newNode = pageHBox.getChildren().get(index);
				newNode.getStyleClass().add(CURRENT_PAGE_BUTTON);
				currentSelectedPageButton = index;

				if (newNode instanceof Button && connect.getEditMode() == DesktopEditMode.PAGE) { // Nur bei Drag And Drop mode
					Button button = (Button) newNode;
					DesktopPageEditButtonView editBox = new DesktopPageEditButtonView(this, mainViewController, openProject.getPage(index));
					button.setGraphic(editBox);
				}
			}
		}
	}

	// EventHandler
	@FXML
	void newDocumentHandler(ActionEvent event) {
		doAction(() ->
		{
			ProjectNewDialog dialog = new ProjectNewDialog(mainViewController.getStage());
			dialog.showAndWait().ifPresent(projectReference -> {
				try {
					PlayPadMain.getProgramInstance().openProject(projectReference, null);
				} catch (DocumentException | IOException | ProjectNotFoundException | ProfileNotFoundException | ProfileAbortException e) {
					Logger.error(e);
				}
			});
		});
	}

	@FXML
	void openDocumentHandler(ActionEvent event) {
		doAction(() ->
		{
			Stage stage = mainViewController.getStage();

			ProjectManagerDialog view = new ProjectManagerDialog(stage);
			Optional<ProjectReference> result = view.showAndWait();

			if (result.isPresent()) {
				ProjectReference ref = result.get();

				try {
					PlayPadMain.getProgramInstance().openProject(ref, null);

					createRecentDocumentMenuItems();
				} catch (ProfileNotFoundException e) {
					Logger.error(e);

					// Error Message
					String errorMessage = Localization.getString(Strings.ERROR_PROFILE_NOT_FOUND, ref.getProfileReference(),
							e.getLocalizedMessage());
					mainViewController.showError(errorMessage);

					// Neues Profile wählen
					ProfileReference profile = null;
					try {
						profile = ProjectReaderDelegateImpl.getInstance(stage).getProfileReference();
					} catch (ProfileAbortException ignored) {
					}
					ref.setProfileReference(profile);
				} catch (ProjectNotFoundException e) {
					Logger.error(e);
					mainViewController.showError(Localization.getString(Strings.ERROR_PROJECT_NOT_FOUND, ref, e.getLocalizedMessage()));
				} catch (Exception e) {
					Logger.error(e);
					mainViewController.showError(Localization.getString(Strings.ERROR_PROJECT_OPEN, ref, e.getLocalizedMessage()));
				}
			}
		});
	}

	@FXML
	void saveMenuHandler(ActionEvent event) {
		mainViewController.save();
	}

	@FXML
	void profileMenuHandler(ActionEvent event) {
		doAction(() ->
		{
			ProfileViewController controller = new ProfileViewController(mainViewController.getStage(), openProject);
			controller.getStageContainer().ifPresent(NVCStage::showAndWait);
			mainViewController.updateWindowTitle();
		});
	}

	@FXML
	void printMenuHandler(ActionEvent event) {
		PrintDialog dialog = new PrintDialog(openProject, mainViewController.getStage());
		dialog.getStageContainer().ifPresent(NVCStage::show);
	}

	@FXML
	void logoutMenuHandler(ActionEvent event) {
		AuthViewController authViewController = new AuthViewController(getContainingWindow(), Localization.getString(Strings.AUTH_LOGOUT),
				(username, password) -> {
					Session session = Session.load();
					try {
						PlayPadPlugin.getServerHandler().getServer().logout(username, password, session.getKey());
						session.delete();
						Platform.exit();
						return true;
					} catch (SessionNotExistsException e) {
						return false;
					}
				});
		authViewController.showStage();
	}

	@FXML
	void playMenuHandler(ActionEvent event) {
		connect.setEditMode(DesktopEditMode.PLAY);
	}

	@FXML
	void dragMenuHandler(ActionEvent event) {
		connect.setEditMode(DesktopEditMode.DRAG);
	}

	@FXML
	void pageMenuHandler(ActionEvent event) {
		connect.setEditMode(DesktopEditMode.PAGE);
	}

	@FXML
	void colorMenuHandler(ActionEvent event) {
		connect.setEditMode(DesktopEditMode.COLOR);
	}

	@FXML
	void notFoundMenuHandler(ActionEvent event) {
		showNotMediaFoundDialog();
	}

	@FXML
	void pluginMenuItemHandler(ActionEvent event) {
		doAction(() ->
		{
			ModernPluginViewController controller = new ModernPluginViewController(mainViewController.getStage());
			controller.getStageContainer().ifPresent(NVCStage::showAndWait);
		});
	}

	@FXML
	void projectSettingsHandler(ActionEvent event) {
		if (projectSettingsViewController == null) {
			Stage mainStage = mainViewController.getStage();

			Runnable onFinish = () -> projectSettingsViewController = null;

			projectSettingsViewController = new ProjectSettingsViewController(mainViewController.getScreen(), mainStage, openProject, onFinish);
			projectSettingsViewController.showStage();
		} else {
			projectSettingsViewController.getStageContainer().ifPresent(stage -> {
				if (stage.getStage().isShowing()) {
					stage.getStage().toFront();
				}
			});
		}
	}

	@FXML
	void profileSettingsHandler(ActionEvent event) {
		Project currentProject = PlayPadMain.getProgramInstance().getCurrentProject();
		GlobalSettings settings = PlayPadPlugin.getInstance().getGlobalSettings();
		if (settings.isLiveMode() && settings.isLiveModeSettings() && currentProject.getActivePlayers() > 0) {
			return;
		}

		if (profileSettingsViewController == null) {
			Stage mainStage = mainViewController.getStage();

			Runnable onFinish = () -> {
				profileSettingsViewController = null;
				mainStage.toFront();
			};

			profileSettingsViewController = new ProfileSettingsViewController(mainStage, openProject, onFinish);
			profileSettingsViewController.showStage();
		} else {
			profileSettingsViewController.getStageContainer().ifPresent(stage -> {
				if (stage.getStage().isShowing()) {
					stage.getStage().toFront();
				}
			});
		}
	}

	@FXML
	void globalSettingsHandler(ActionEvent event) {
		if (globalSettingsViewController == null) {

			Stage mainStage = mainViewController.getStage();
			Runnable onFinish = () -> {
				globalSettingsViewController = null;
				mainStage.toFront();
			};

			globalSettingsViewController = new GlobalSettingsViewController(mainStage, onFinish);
			globalSettingsViewController.getStageContainer().ifPresent(NVCStage::show);
		} else {
			globalSettingsViewController.getStageContainer().ifPresent(stage -> {
				if (stage.getStage().isShowing()) {
					stage.getStage().toFront();
				}
			});
		}
	}

	@FXML
	void addColumnToProject() {
		ProjectSettingsValidator projectSettingsValidator = new ProjectSettingsValidator(mainViewController.getScreen());
		final int maxValue = projectSettingsValidator.maxValue(ProjectSettingsValidator.Dimension.COLUMNS);
		if (maxValue < openProject.getSettings().getColumns() + 1) {
			showErrorMessage(Localization.getString("Error.Screen.TooMuch", maxValue));
			return;
		}

		openProject.addColumn();
		mainViewController.createPadViews();
		mainViewController.showPage(mainViewController.getPage());
	}

	@FXML
	void addRowToProject() {
		ProjectSettingsValidator projectSettingsValidator = new ProjectSettingsValidator(mainViewController.getScreen());
		final int maxValue = projectSettingsValidator.maxValue(ProjectSettingsValidator.Dimension.ROWS);
		if (maxValue < openProject.getSettings().getRows() + 1) {
			showErrorMessage(Localization.getString("Error.Screen.TooMuch", maxValue));
			return;
		}

		openProject.addRow();
		mainViewController.createPadViews();
		mainViewController.showPage(mainViewController.getPage());
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
		TextField searchField = TextFields.createClearableTextField();
		searchField.setPromptText(Localization.getString(Strings.SEARCH_PLACEHOLDER));

		Button button = new Button(Localization.getString(Strings.SEARCH_BUTTON));
		button.setDefaultButton(true);
		Project project = PlayPadMain.getProgramInstance().getCurrentProject();
		button.setOnAction(new DesktopSearchController(project, searchField, mainViewController));

		HBox box = new HBox(14, searchField, button);
		box.setAlignment(Pos.CENTER_LEFT);

		NotificationPane pane = mainViewController.getNotificationPane();
		pane.setOnShown(e -> searchField.requestFocus());
		pane.show("", box);

		searchField.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ESCAPE) {
				pane.hide();
			}
		});
	}

	@FXML
	void aboutMenuHandler(ActionEvent event) {
		AboutDialog aboutDialog = new AboutDialog(mainViewController.getStage());
		aboutDialog.getStageContainer().ifPresent(NVCStage::show);

	}

	@FXML
	void showChangelogMenuHandler(ActionEvent event) {
		String website = ApplicationUtils.getApplication().getUserInfo(AppUserInfoStrings.class).changelogURL();
		try {
			Desktop.getDesktop().browse(new URI(website));
		} catch (IOException | URISyntaxException e) {
			Logger.error(e);
		}
	}

	@FXML
	void visitWebsiteMenuHandler(ActionEvent event) {
		String website = ApplicationUtils.getApplication().getUserInfo(AppUserInfoStrings.class).website();
		try {
			Desktop.getDesktop().browse(new URI(website));
		} catch (IOException | URISyntaxException e) {
			Logger.error(e);
		}
	}

	private static final int LAST_DOCUMENT_LIMIT = 3;

	private void createRecentDocumentMenuItems() {
		recentOpenMenu.getItems().clear();

		String project = openProject.getProjectReference().getName();

		ProjectReferenceManager.getProjectsSorted().stream().filter(item -> !item.getName().equals(project)).limit(LAST_DOCUMENT_LIMIT).forEach(item ->
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
			handlePageButton(event);
		} else if (event.getSource() instanceof MenuItem) {
			handleLastOpenProjectMenuItems(event);
		}
	}

	private void handleLastOpenProjectMenuItems(ActionEvent event) {
		doAction(() ->
		{
			// TODO Rewrite mit openProject von BasicMenuToolbarViewController
			MenuItem item = (MenuItem) event.getSource();
			ProjectReference ref = (ProjectReference) item.getUserData();

			try {
				// Speichern das alte Project in mvc.setProject(Project)
				PlayPadMain.getProgramInstance().openProject(ref, null);
			} catch (ProfileNotFoundException e) {
				Logger.error(e);
				mainViewController.showError(
						Localization.getString(Strings.ERROR_PROFILE_NOT_FOUND, ref.getProfileReference(), e.getLocalizedMessage()));

				// Neues Profile wählen
				ProfileReference profile = null;
				try {
					profile = ProjectReaderDelegateImpl.getInstance(getContainingWindow()).getProfileReference();
				} catch (ProfileAbortException ignored) {
				}
				ref.setProfileReference(profile);
			} catch (ProjectNotFoundException e) {
				Logger.error(e);
				mainViewController.showError(Localization.getString(Strings.ERROR_PROJECT_NOT_FOUND, ref, e.getLocalizedMessage()));
			} catch (Exception e) {
				Logger.error(e);
				mainViewController.showError(Localization.getString(Strings.ERROR_PROJECT_OPEN, ref, e.getLocalizedMessage()));
			}
		});
	}

	private void handlePageButton(ActionEvent event) {
		Button button = (Button) event.getSource();
		int page = (int) button.getUserData();
		mainViewController.showPage(page);
	}
}
