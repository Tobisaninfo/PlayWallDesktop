package de.tobias.playpad.layout.touch;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.ProjectNotFoundException;
import de.tobias.playpad.project.ProjectReference;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileNotFoundException;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.playpad.view.main.MainLayoutConnect;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.dialog.ImportDialog;
import de.tobias.playpad.viewcontroller.main.BasicMenuToolbarViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;

public class TouchMenuToolbarViewController extends BasicMenuToolbarViewController {

	@FXML protected CheckMenuItem fullScreenMenuItem;
	@FXML protected CheckMenuItem alwaysOnTopItem;
	@FXML protected MenuItem closeMenuItem;

	private IMainViewController mainViewController;

	public TouchMenuToolbarViewController(IMainViewController mainViewController) {
		super("header", "de/tobias/playpad/assets/view/main/touch/", PlayPadMain.getUiResourceBundle(), mainViewController);
		this.mainViewController = mainViewController;

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
			button.setFocusTraversable(false);
			button.setOnAction(this);
			pageHBox.getChildren().add(button);
		}
	}

	@Override
	public void setLocked(boolean looked) {

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
	public void addToolbarIcon(Image icon) {
		// TODO Implement
	}

	@Override
	public void removeToolbarIcon(Image icon) {
		// TODO Implement
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
	public void addMenuItem(MenuItem item, MenuType type) {}

	@Override
	public void removeMenuItem(MenuItem item) {}

	@Override
	public boolean isAlwaysOnTopActive() {
		return false; // TODO
	}

	@Override
	public boolean isFullscreenActive() {
		return false; // TODO
	}

	@Override
	public void deinit() {
		toolbarHBox.prefWidthProperty().unbind();
		toolbarHBox.prefHeightProperty().unbind();
	}

	@Override
	public Slider getVolumeSlider() {
		return volumeSlider;
	}

	// Event Handler
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
	void closeMenuItemHandler(ActionEvent event) {
		MainLayoutConnect defaultLayout = PlayPadPlugin.getRegistryCollection().getMainLayouts().getDefault();

		Profile.currentProfile().getProfileSettings().setMainLayoutType(defaultLayout.getType());
		mainViewController.setMainLayout(defaultLayout);
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