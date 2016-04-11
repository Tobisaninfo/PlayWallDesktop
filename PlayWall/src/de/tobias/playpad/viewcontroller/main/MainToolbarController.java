package de.tobias.playpad.viewcontroller.main;

import java.net.URL;
import java.util.ResourceBundle;

import de.tobias.playpad.Strings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.viewcontroller.main.IMainToolbarViewController;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;

public class MainToolbarController implements IMainToolbarViewController, Initializable, EventHandler<ActionEvent> {

	@FXML private ToolBar toolbar;
	@FXML private HBox toolbarHBox;
	@FXML private HBox pageHBox;

	@FXML private Label volumeDownLabel;
	@FXML private Slider volumeSlider;
	@FXML private Label volumeUpLabel;

	private MainViewController mainViewController;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// HBox Child wird Max Width, subtract weil sonst zu groß für Toolbar
		toolbarHBox.prefWidthProperty().bind(toolbar.widthProperty().subtract(25));
		toolbarHBox.prefHeightProperty().bind(toolbar.minHeightProperty());

		// Icons Volume
		volumeDownLabel.setGraphic(new FontIcon("volume-item", FontAwesomeType.VOLUME_DOWN));
		volumeUpLabel.setGraphic(new FontIcon("volume-item", FontAwesomeType.VOLUME_UP));

		volumeSlider.setOnScroll(ev ->
		{
			volumeSlider.setValue(volumeSlider.getValue() - ev.getDeltaY() * 0.001);
			volumeSlider.setValue(volumeSlider.getValue() + ev.getDeltaX() * 0.001);
		});
	}

	public void createPageButtons() {
		pageHBox.getChildren().clear();
		for (int i = 0; i < Profile.currentProfile().getProfileSettings().getPageCount(); i++) {
			Button item = new Button(Localization.getString(Strings.UI_Window_Main_PageButton, (i + 1)));
			item.setUserData(i);
			item.setFocusTraversable(false);
			item.setOnAction(this);
			pageHBox.getChildren().add(item);
		}
	}

	public HBox getPageHBox() {
		return pageHBox;
	}

	public HBox getToolbarHBox() {
		return toolbarHBox;
	}

	public Slider getVolumeSlider() {
		return volumeSlider;
	}

	public void setMainViewController(MainViewController mainViewController) {
		this.mainViewController = mainViewController;
	}

	@Override
	public void handle(ActionEvent event) {
		if (event.getSource() instanceof Button) {
			// Page Buttons
			Button item = (Button) event.getSource();
			int number = (int) item.getUserData();
			mainViewController.showPage(number);
		}
	}
}
