package de.tobias.playpad.layout.desktop;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ProjectReference;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.dialog.ImportDialog;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;

public class DesktopMenuToolbarViewController extends BasicMenuToolbarViewController implements EventHandler<ActionEvent> {

	private IMainViewController mainViewController;

	public DesktopMenuToolbarViewController(IMainViewController controller) {
		super("header", "de/tobias/playpad/assets/view/main/desktop/", PlayPadMain.getUiResourceBundle(), controller);
		this.mainViewController = controller;

		toolbarHBox.prefWidthProperty().bind(toolbar.widthProperty().subtract(25));
		toolbarHBox.prefHeightProperty().bind(toolbar.minHeightProperty());
	}

	@Override
	public void initPageButtons() {
		pageHBox.getChildren().clear();

		ProfileSettings settings = Profile.currentProfile().getProfileSettings();

		for (int i = 0; i < settings.getPageCount(); i++) {
			Button button = new Button(Localization.getString(Strings.UI_Window_Main_PageButton, (i + 1)));
			button.setUserData(i);
			button.setOnAction(this);
			pageHBox.getChildren().add(button);
		}
	}

	@Override
	public void setLocked(boolean looked) {
		// TODO Implement
	}

	@Override
	public void addToolbarIcon(Image icon) {
		// TODO Implement
	}

	@Override
	public void removeToolbarIcon(Image icon) {
		// TODO Implement
	}

	@Override
	public void addMenuItem(MenuItem item, MenuType type) {
		// TODO Implement
	}

	@Override
	public void removeMenuItem(MenuItem item) {
		// TODO Implement
	}

	@Override
	public boolean isAlwaysOnTopActive() {
		// TODO Imeplement
		return false;
	}

	@Override
	public boolean isFullscreenActive() {
		//TODO Implement
		return false;
	}

	@Override
	public void deinit() {
		// TODO Implement
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
					Project project = Project.load(ref, true, ImportDialog.getInstance(mainViewController.getStage()));
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
