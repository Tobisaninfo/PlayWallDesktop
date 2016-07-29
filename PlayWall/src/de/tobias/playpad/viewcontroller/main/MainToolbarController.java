package de.tobias.playpad.viewcontroller.main;

import java.net.URL;
import java.util.ResourceBundle;

import de.tobias.playpad.Strings;
import de.tobias.playpad.settings.Profile;
import de.tobias.playpad.settings.ProfileListener;
import de.tobias.playpad.settings.ProfileSettings;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;

@Deprecated
public class MainToolbarController implements IMainToolbarViewController, Initializable, EventHandler<ActionEvent>, ProfileListener {

	@FXML private ToolBar toolbar;
	@FXML private HBox toolbarHBox;
	@FXML private HBox pageHBox;

	@FXML private HBox iconHbox;
	private Label lockedLabel;

	@FXML private Label volumeDownLabel;
	@FXML private Slider volumeSlider;
	@FXML private Label volumeUpLabel;

	private MainViewController mainViewController;

	private ChangeListener<Boolean> lockedListener;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();
		Profile.registerListener(this);

		// Listener
		lockedListener = new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					iconHbox.getChildren().add(lockedLabel);
				} else {
					iconHbox.getChildren().remove(lockedLabel);
				}
			}
		};
		profileSettings.lockedProperty().addListener(lockedListener);

		// HBox Child wird Max Width, subtract weil sonst zu groß für Toolbar
		toolbarHBox.prefWidthProperty().bind(toolbar.widthProperty().subtract(25));
		toolbarHBox.prefHeightProperty().bind(toolbar.minHeightProperty());

		// Info Icons
		lockedLabel = new Label();
		lockedLabel.setGraphic(new FontIcon(FontAwesomeType.LOCK));
		lockedListener.changed(profileSettings.lockedProperty(), null, profileSettings.isLocked());

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

	@Override
	public void reloadSettings(Profile oldProfile, Profile currentProfile) {
		if (oldProfile != null) {
			oldProfile.getProfileSettings().lockedProperty().removeListener(lockedListener);
		}
		ProfileSettings profileSettings = currentProfile.getProfileSettings();
		profileSettings.lockedProperty().addListener(lockedListener);
		lockedListener.changed(profileSettings.lockedProperty(), null, profileSettings.isLocked());
	}

	@Override
	public void showIcon(Node node) {
		iconHbox.getChildren().add(node);
	}

	@Override
	public void hideIcon(Node node) {
		iconHbox.getChildren().remove(node);	
	}

}
