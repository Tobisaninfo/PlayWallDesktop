package de.tobias.playpad.layout.touch;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.settings.keys.KeyCollection;
import de.tobias.playpad.view.main.MainLayoutFactory;
import de.tobias.playpad.view.main.MenuType;
import de.tobias.playpad.viewcontroller.main.BasicMenuToolbarViewController;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.nui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;

public class TouchMenuToolbarViewController extends BasicMenuToolbarViewController {

	@FXML
	protected Button closeButton;

	@FXML
	protected Label liveLabel;

	private IMainViewController mainViewController;

	TouchMenuToolbarViewController(IMainViewController mainViewController) {
		super("header", "de/tobias/playpad/assets/view/main/touch/", PlayPadMain.getUiResourceBundle());
		this.mainViewController = mainViewController;

		// Schriftgröße Icons
		FontIcon icon = (FontIcon) volumeDownLabel.getGraphic();
		icon.setSize(35);
		icon = (FontIcon) volumeUpLabel.getGraphic();
		icon.setSize(35);
	}

	@Override
	public void init() {
		super.init();
		volumeSlider.focusedProperty().addListener(i -> mainViewController.getParent().requestFocus());
	}

	@Override
	public void initPageButtons() {
		pageHBox.getChildren().clear();

		if (openProject == null) {
			return;
		}

		for (int i = 0; i < openProject.getPages().size(); i++) {
			Page page = openProject.getPage(i);

			Button button = new Button();
			StringBinding nameBinding = Bindings.when(page.nameProperty().isEmpty())
					.then(Localization.getString(Strings.UI_Window_Main_PageButton, (i + 1)))
					.otherwise(page.nameProperty());
			button.textProperty().bind(nameBinding);
			button.setUserData(i);
			button.setFocusTraversable(false);
			button.setOnAction(this);
			pageHBox.getChildren().add(button);
		}
	}

	@Override
	public void setOpenProject(Project project) {
		super.setOpenProject(project);

		liveLabel.visibleProperty().unbind();
		if (project != null) {
			liveLabel.visibleProperty().bind(project.activePlayerProperty().greaterThan(0));
		}
	}

	@Override
	public void loadKeybinding(KeyCollection keys) {
	}

	@Override
	public void setLocked(boolean looked) {
	}

	@Override
	public void setAlwaysOnTopActive(boolean alwaysOnTopActive) {
	}

	@Override
	public void setFullScreenActive(boolean fullScreenActive) {
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
	}

	@Override
	public void removeMenuItem(MenuItem item) {
	}

	@Override
	public void deinit() {
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

	@Override
	public Slider getVolumeSlider() {
		return volumeSlider;
	}

	// Event Handler
	@FXML
	void closeMenuItemHandler(ActionEvent event) {
		MainLayoutFactory defaultLayout = PlayPadPlugin.getRegistryCollection().getMainLayouts().getDefault();

		Profile.currentProfile().getProfileSettings().setMainLayoutType(defaultLayout.getType());
		mainViewController.setMainLayout(defaultLayout);
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() instanceof Button) {
			Button button = (Button) event.getSource();
			int page = (int) button.getUserData();
			mainViewController.showPage(page);
		}
	}

}