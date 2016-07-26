package de.tobias.playpad.viewcontroller.main;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import de.tobias.playpad.AppUserInfoStrings;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.action.Action;
import de.tobias.playpad.action.cartaction.CartAction;
import de.tobias.playpad.action.connect.CartActionConnect;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.pad.view.IPadViewController;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ProjectReference;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileListener;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.viewcontroller.SettingsTabViewController;
import de.tobias.playpad.viewcontroller.dialog.ErrorSummaryDialog;
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
import de.tobias.utils.util.Localization;
import de.tobias.utils.util.OS;
import de.tobias.utils.util.OS.OSType;
import de.tobias.utils.util.Worker;
import de.tobias.utils.util.net.FileUpload;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainMenuBarController implements EventHandler<ActionEvent>, Initializable, ProfileListener {

	@FXML private MenuBar menuBar;
	@FXML CheckMenuItem dndModeMenuItem;
	@FXML CheckMenuItem alwaysOnTopItem;
	@FXML CheckMenuItem fullScreenMenuItem;
	@FXML Menu recentOpenMenu;
	@FXML MenuItem profileMenu;

	@FXML CheckMenuItem quickEditMenuItem;
	@FXML MenuItem settingsMenuItem;

	@FXML Menu extensionMenu;

	// Open Windows
	private SettingsViewController settingsViewController;
	private MainViewController mvc;

	private ChangeListener<Boolean> lockedListener;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		menuBar.setUseSystemMenuBar(true);

		if (OS.getType() == OSType.MacOSX) {
			menuBar.setMaxHeight(0);
		}

		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();
		Profile.registerListener(this); // Update, wenn sich das Profil ändert (remove Listener & add Listener)

		// Listener
		lockedListener = new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				dndModeMenuItem.setDisable(newValue);
			}
		};
		profileSettings.lockedProperty().addListener(lockedListener);
		lockedListener.changed(profileSettings.lockedProperty(), null, profileSettings.isLocked());
	}

	// Event
	@FXML
	private void newDocumentHandler(ActionEvent event) {
		doAction(() ->
		{
			NewProjectDialog dialog = new NewProjectDialog(mvc.getStage());
			dialog.getStage().showAndWait();

			Project project = dialog.getProject();
			if (project != null) {
				mvc.setProject(project);
				mvc.showPage(0);
			}
		});
	}

	@FXML
	private void openDocumentHandler(ActionEvent event) {
		doAction(() ->
		{
			ProjectManagerDialog view = new ProjectManagerDialog(mvc.getStage(), mvc.getProject());
			Optional<ProjectReference> result = view.showAndWait();
			if (result.isPresent()) {
				ProjectReference ref = result.get();
				try {
					Project project = Project.load(result.get(), true, ImportDialog.getInstance(mvc.getStage()));
					mvc.setProject(project);

					mvc.showPage(0);
					createRecentDocumentMenuItems();
				} catch (ProfileNotFoundException e) {
					e.printStackTrace();
					mvc.showErrorMessage(
							Localization.getString(Strings.Error_Profile_NotFound, ref.getProfileReference(), e.getLocalizedMessage()));

					// Neues Profile wählen
					Profile profile = ImportDialog.getInstance(mvc.getStage()).getUnkownProfile();
					ref.setProfileReference(profile.getRef());
				} catch (ProjectNotFoundException e) {
					e.printStackTrace();
					mvc.showErrorMessage(Localization.getString(Strings.Error_Project_NotFound, ref, e.getLocalizedMessage()));
				} catch (Exception e) {
					e.printStackTrace();
					mvc.showErrorMessage(Localization.getString(Strings.Error_Project_Open, ref, e.getLocalizedMessage()));
				}
			}
		});
	}

	@FXML
	private void saveMenuHandler(ActionEvent event) {
		try {
			mvc.getProject().save();
			mvc.notify(Localization.getString(Strings.Standard_File_Save), PlayPadMain.displayTimeMillis);
		} catch (IOException e) {
			mvc.showError(Localization.getString(Strings.Error_Project_Save));
			e.printStackTrace();
		}
	}

	// Einstellungsmenu
	@FXML
	private void profileMenuHandler(ActionEvent event) {
		doAction(() ->
		{
			ProfileViewController controller = new ProfileViewController(mvc.getStage(), mvc.getProject());
			controller.getStage().showAndWait();
			mvc.setTitle();
		});
	}

	@FXML
	private void quickEditMenuHandler(ActionEvent event) {
		try {
			for (Action action : Profile.currentProfile().getMappings().getActiveMapping().getActionsOfType(CartActionConnect.TYPE)) {
				CartAction cartAction = (CartAction) action;
				if (cartAction.getCart() < mvc.padViewList.size()) {
					cartAction.getPad().getController().getParent().setBusy(quickEditMenuItem.isSelected());
					// IPadViewController controller = mvc.padViewList.get(cartAction.getCart());
					// MapperQuickSettingsView view = new MapperQuickSettingsView((Pane) controller.getParent().getParent());
					// view.showDropOptions(action.getMappers());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void settingsHandler(ActionEvent event) {
		Midi midi = Midi.getInstance();
		Project project = mvc.getProject();
		ProfileSettings settings = Profile.currentProfile().getProfileSettings();

		if (settings.isLiveMode() && settings.isLiveModeSettings() && project.getPlayedPlayers() > 0) {
			mvc.showLiveInfo();
			return;
		}

		if (settingsViewController == null) {
			settingsViewController = new SettingsViewController(midi, mvc.getScreen(), mvc.getStage(), project) {

				@Override
				public void updateData() {
					midi.setListener(mvc.getMidiHandler());

					boolean change = false;
					for (SettingsTabViewController controller : tabs) {
						if (controller.needReload()) {
							change = true;
							controller.reload(Profile.currentProfile(), project, mvc);
						}
					}

					if (change) {
						PlayPadPlugin.getImplementation().getSettingsListener().forEach(l -> l.onChange(Profile.currentProfile()));
					}

					settingsViewController = null;
					mvc.getStage().toFront();
				}
			};

			settingsViewController.getStage().show();
		} else if (settingsViewController.getStage().isShowing()) {
			settingsViewController.getStage().toFront();
		}
	}

	@FXML
	private void printMenuHandler(ActionEvent event) {
		PrintDialog dialog = new PrintDialog(mvc.getProject(), mvc.getStage());
		dialog.getStage().show();
	}

	@FXML
	private void alwaysOnTopItemHandler(ActionEvent event) {
		mvc.getStage().setAlwaysOnTop(alwaysOnTopItem.isSelected());
		Profile.currentProfile().getProfileSettings().setWindowAlwaysOnTop(alwaysOnTopItem.isSelected());
	}

	@FXML
	private void fullScreenMenuItemHandler(ActionEvent event) {
		mvc.getStage().setFullScreen(fullScreenMenuItem.isSelected());
	}

	@FXML
	private void dndModeHandler(ActionEvent event) {
		if (dndModeMenuItem.isSelected()) {
			ProfileSettings settings = Profile.currentProfile().getProfileSettings();
			if (settings.isLiveMode() && settings.isLiveModeDrag() && mvc.getProject().getPlayedPlayers() > 0) {
				mvc.showLiveInfo();
			} else {
				PadDragListener.setDndMode(true);
				for (IPadViewController view : mvc.padViewList) {
					view.showDnDLayout(true);
				}
			}
		} else {
			PadDragListener.setDndMode(false);
			for (IPadViewController view : mvc.padViewList) {
				view.showDnDLayout(false);
			}
		}

	}

	@FXML
	private void errorMenuHandler(ActionEvent event) {
		ErrorSummaryDialog.getInstance().getStage().show();
	}

	@FXML
	private void aboutMenuHandler(ActionEvent event) {
		ApplicationInfo info = ApplicationUtils.getApplication().getInfo();
		String message = Localization.getString(Strings.UI_Dialog_Info_Content, info.getVersion(), info.getBuild(), info.getAuthor());
		mvc.showInfoMessage(message, Localization.getString(Strings.UI_Dialog_Info_Header, info.getName()), PlayPadMain.stageIcon.orElse(null));
	}

	@FXML
	private void visiteWebsiteMenuHandler(ActionEvent event) {
		String website = ApplicationUtils.getApplication().getInfo().getUserInfo().getString(AppUserInfoStrings.WEBSITE);
		try {
			Desktop.getDesktop().browse(new URI(website));
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void sendErrorMenuItem(ActionEvent event) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.initOwner(mvc.getStage());
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

	@FXML
	private void pluginMenuItemHandler(ActionEvent e) {
		doAction(() ->
		{
			PluginViewController controller = new PluginViewController(mvc.getStage());
			controller.getStage().showAndWait();
			mvc.showPage(mvc.getPage());
		});
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() instanceof MenuItem) {
			// Zuletzt verwendete Projects
			doAction(() ->
			{
				MenuItem item = (MenuItem) event.getSource();
				ProjectReference ref = (ProjectReference) item.getUserData();
				try {
					// Speichern das alte Project in mvc.setProject(Project)
					Project project = Project.load(ref, true, ImportDialog.getInstance(mvc.getStage()));
					mvc.setProject(project);
					mvc.showPage(0);
				} catch (ProfileNotFoundException e) {
					e.printStackTrace();
					mvc.showErrorMessage(
							Localization.getString(Strings.Error_Profile_NotFound, ref.getProfileReference(), e.getLocalizedMessage()));

					// Neues Profile wählen
					Profile profile = ImportDialog.getInstance(mvc.getStage()).getUnkownProfile();
					ref.setProfileReference(profile.getRef());
				} catch (ProjectNotFoundException e) {
					e.printStackTrace();
					mvc.showErrorMessage(Localization.getString(Strings.Error_Project_NotFound, ref, e.getLocalizedMessage()));
				} catch (Exception e) {
					e.printStackTrace();
					mvc.showErrorMessage(Localization.getString(Strings.Error_Project_Open, ref, e.getLocalizedMessage()));
				}
			});
		}
	}

	private void doAction(Runnable run) {
		if (mvc.getProject().getPlayedPlayers() > 0 && Profile.currentProfile().getProfileSettings().isLiveMode()) {
			mvc.showLiveInfo();
		} else {
			run.run();
		}
	}

	private final int LAST_DOCUMENT_LIMIT = 3;

	public void createRecentDocumentMenuItems() {
		recentOpenMenu.getItems().clear();

		String project = mvc.getProject().getRef().getName();

		ProjectReference.getProjectsSorted().stream().filter(item -> !item.getName().equals(project)).limit(LAST_DOCUMENT_LIMIT).forEach(item ->
		{
			MenuItem menuItem = new MenuItem(item.toString());
			menuItem.setUserData(item);
			menuItem.setOnAction(this);
			recentOpenMenu.getItems().add(menuItem);
		});
	}

	public void setMainViewController(MainViewController mvc) {
		this.mvc = mvc;
	}

	@Override
	public void reloadSettings(Profile oldProfile, Profile currentProfile) {
		if (oldProfile != null) {
			oldProfile.getProfileSettings().lockedProperty().removeListener(lockedListener);
		}
		ProfileSettings profileSettings = currentProfile.getProfileSettings();
		profileSettings.lockedProperty().addListener(lockedListener);
		lockedListener.changed(profileSettings.lockedProperty(), null, profileSettings.isLocked());
	}
}
