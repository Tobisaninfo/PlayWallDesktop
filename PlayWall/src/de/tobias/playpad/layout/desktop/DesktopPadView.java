package de.tobias.playpad.layout.desktop;

import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.pad.viewcontroller.IPadViewControllerV2;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.ui.scene.BusyView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class DesktopPadView implements IPadViewV2 {

	private Label indexLabel;
	private Label loopLabel;
	private Label triggerLabel;
	private Label errorLabel;

	private HBox infoBox;
	private Label timeLabel;

	private HBox preview;
	private IPadContentView previewContent;

	private ProgressBar playBar;
	private Button playButton;
	private Button pauseButton;
	private Button stopButton;
	private Button newButton;
	private Button settingsButton;
	private HBox buttonBox;

	private StackPane superRoot;
	private VBox root;
	private BusyView busyView;

	private transient DesktopPadViewController controller; // Reference to its controller

	public DesktopPadView() {
		controller = new DesktopPadViewController();
		setupView();
	}
	
	private void setupView() {
		superRoot = new StackPane();
		root = new VBox();
		busyView = new BusyView(superRoot);

		indexLabel = new Label();

		loopLabel = new Label(); // Active über Visible
		loopLabel.setGraphic(new FontIcon(FontAwesomeType.REPEAT));

		triggerLabel = new Label();
		triggerLabel.setGraphic(new FontIcon(FontAwesomeType.EXTERNAL_LINK));

		errorLabel = new Label();
		errorLabel.setGraphic(new FontIcon(FontAwesomeType.WARNING));

		timeLabel = new Label();

		infoBox = new HBox(); // childern in addDefaultButton()
		infoBox.setSpacing(5);

		preview = new HBox();
		HBox.setHgrow(preview, Priority.ALWAYS);
		VBox.setVgrow(preview, Priority.ALWAYS);

		HBox.setHgrow(timeLabel, Priority.ALWAYS);
		timeLabel.setMaxWidth(Double.MAX_VALUE);
		timeLabel.setAlignment(Pos.CENTER_RIGHT);

		playBar = new ProgressBar(0);
		playBar.prefWidthProperty().bind(root.widthProperty());

		// Buttons
		playButton = new Button("", new FontIcon(FontAwesomeType.PLAY));
		playButton.setFocusTraversable(false);
		playButton.setOnAction(controller);
		pauseButton = new Button("", new FontIcon(FontAwesomeType.PAUSE));
		pauseButton.setFocusTraversable(false);
		pauseButton.setOnAction(controller);
		stopButton = new Button("", new FontIcon(FontAwesomeType.STOP));
		stopButton.setFocusTraversable(false);
		stopButton.setOnAction(controller);
		newButton = new Button("", new FontIcon(FontAwesomeType.FOLDER_OPEN));
		newButton.setFocusTraversable(false);
		newButton.setOnAction(controller);
		settingsButton = new Button("", new FontIcon(FontAwesomeType.GEAR));
		settingsButton.setFocusTraversable(false);
		settingsButton.setOnAction(controller);

		// Button HBOX
		buttonBox = new HBox(); // childern in addDefaultButton()

		root.getChildren().addAll(infoBox, preview, playBar, buttonBox);
		superRoot.getChildren().addAll(root);
	}
	
	@Override
	public IPadContentView getContentView() {
		return previewContent;
	}

	@Override
	public void setContentView(IPadContentView contentView) {
		previewContent.deinit();
		
		this.previewContent = contentView;
	}

	@Override
	public IPadViewControllerV2 getViewController() {
		return controller;
	}

	@Override
	public Node getRootNode() {
		return superRoot;
	}

	@Override
	public void enableDragAndDropDesignMode(boolean enable) {

	}
	
	@Override
	public void showBusyView(boolean enable) {
		busyView.showProgress(enable);
	}

}
