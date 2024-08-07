package de.tobias.playpad.viewcontroller.main;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.settings.GlobalSettings;
import de.tobias.playpad.viewcontroller.dialog.PathMatchDialog;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

import java.util.ResourceBundle;

public abstract class BasicMenuToolbarViewController extends MenuToolbarViewController implements EventHandler<ActionEvent> {

	// Menu
	@FXML
	protected Label volumeUpLabel;
	@FXML
	protected HBox iconHbox;

	@FXML
	protected FlowPane pageButtonPage;
	@FXML
	protected HBox toolbarHBox;
	@FXML
	protected Menu recentOpenMenu;
	@FXML
	protected Slider volumeSlider;
	@FXML
	protected Label volumeDownLabel;

	@FXML
	private HBox notFoundContainer;
	@FXML
	private Label notFoundLabel;


	protected Project openProject; // REFERENCE zu MainViewController

	protected BasicMenuToolbarViewController(String name, String path, ResourceBundle localization) {
		super(name, path, localization);
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

		volumeSlider.styleProperty().bind(Bindings.createStringBinding(() -> {

			double min = volumeSlider.getMin();
			double max = volumeSlider.getMax();
			double value = volumeSlider.getValue();

			return createSliderStyle(min, max, value);

		}, volumeSlider.valueProperty()));

		FontIcon fontIcon = new FontIcon(FontAwesomeType.EXCLAMATION_TRIANGLE);
		fontIcon.getStyleClass().add("pad-notfound");
		fontIcon.setSize(20);
		fontIcon.setOnMouseClicked(e -> showNotMediaFoundDialog());

		notFoundContainer.getChildren().add(0, fontIcon);
	}

	public void showNotMediaFoundDialog() {
		PathMatchDialog dialog = new PathMatchDialog(openProject, getContainingWindow());
		dialog.showAndWait();
	}

	@Override
	public void setNotFoundNumber(int count) {
		notFoundContainer.setVisible(count > 0);
		notFoundLabel.setText(String.valueOf(count));
	}

	// Utils
	protected void doAction(Runnable run) {
		Project project = PlayPadMain.getProgramInstance().getCurrentProject();
		GlobalSettings globalSettings = PlayPadPlugin.getInstance().getGlobalSettings();
		if (!(project.getActivePlayers() > 0 && globalSettings.isLiveMode())) {
			run.run();
		}
	}

	@Override
	public void setOpenProject(Project project) {
		this.openProject = project;
	}

	@SuppressWarnings("StringBufferReplaceableByString")
	private String createSliderStyle(double min, double max, double value) {
		StringBuilder gradient = new StringBuilder("-slider-track-color: ");
		String defaultBG = "#5c5c5c ";
		gradient.append("linear-gradient(to right, ").append(defaultBG).append("0%, ");

		double valuePercent = 100.0 * (value - min) / (max - min);

		gradient.append(defaultBG).append(min).append("%, ");
		gradient.append("#358dab ").append(min).append("%, ");
		gradient.append("#358dab ").append(valuePercent).append("%, ");
		gradient.append(defaultBG).append(valuePercent).append("%, ");
		gradient.append(defaultBG).append("100%); ");

		return gradient.toString();
	}

	protected Button createPageButton(Page page, int index) {
		Button button = new Button();

		StringBinding nameBinding = Bindings.when(page.nameProperty().isEmpty())
				.then(Localization.getString(Strings.UI_WINDOW_MAIN_PAGE_BUTTON, (index + 1)))
				.otherwise(page.nameProperty());
		button.textProperty().bind(nameBinding);
		button.setUserData(index);
		button.setFocusTraversable(false);
		button.setOnAction(this);

		return button;
	}

}
