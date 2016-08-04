package de.tobias.playpad.layout.desktop;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.pad.conntent.play.Pauseable;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.pad.view.IPadViewV2;
import de.tobias.playpad.pad.viewcontroller.IPadViewControllerV2;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.view.EmptyPadView;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.ui.scene.BusyView;
import javafx.beans.property.Property;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
		controller = new DesktopPadViewController(this);
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
	public void setContentView(Pad pad) {
		if (previewContent != null) {
			previewContent.deinit();
		}

		if (pad != null) {
			PadContent content = pad.getContent();
			if (content != null) {
				try {
					PadContentConnect connect = PlayPadPlugin.getRegistryCollection().getPadContents().getComponent(content.getType());
					previewContent = connect.getPadContentPreview(pad, preview);
					Node node = previewContent.getNode();

					node.getStyleClass().addAll("pad-title", "pad" + pad.getIndex() + "-title");
					preview.getChildren().setAll(node);
					return;
				} catch (NoSuchComponentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		EmptyPadView view = new EmptyPadView(preview);
		if (pad != null) {
			view.getStyleClass().addAll("pad-title", "pad" + pad.getIndex() + "-title");
		} else {
			view.getStyleClass().addAll("pad-title");
		}
		preview.getChildren().setAll(view);
	}

	@Override
	public IPadViewControllerV2 getViewController() {
		return controller;
	}

	@Override
	public Pane getRootNode() {
		return superRoot;
	}

	@Override
	public void enableDragAndDropDesignMode(boolean enable) {
		pseudoClassState(PseudoClasses.DRAG_CLASS, enable);
	}

	@Override
	public void showBusyView(boolean enable) {
		busyView.showProgress(enable);
	}

	public void setTitle(String text) {
		this.indexLabel.setText(text);
	}

	public void setProgress(double progress) {
		this.playBar.setProgress(progress);
	}

	public ProgressBar getPlayBar() {
		return playBar;
	}

	@Override
	public void pseudoClassState(PseudoClass pseudoClass, boolean active) {
		superRoot.pseudoClassStateChanged(pseudoClass, active);
		indexLabel.pseudoClassStateChanged(pseudoClass, active);
		timeLabel.pseudoClassStateChanged(pseudoClass, active);
		loopLabel.getGraphic().pseudoClassStateChanged(pseudoClass, active);
		triggerLabel.getGraphic().pseudoClassStateChanged(pseudoClass, active);
		errorLabel.getGraphic().pseudoClassStateChanged(pseudoClass, active);

		if (preview != null) {
			preview.getChildren().forEach(i -> i.pseudoClassStateChanged(pseudoClass, active));
		}

		playBar.pseudoClassStateChanged(pseudoClass, active);

		playButton.getGraphic().pseudoClassStateChanged(pseudoClass, active);
		pauseButton.getGraphic().pseudoClassStateChanged(pseudoClass, active);
		stopButton.getGraphic().pseudoClassStateChanged(pseudoClass, active);
		newButton.getGraphic().pseudoClassStateChanged(pseudoClass, active);
		settingsButton.getGraphic().pseudoClassStateChanged(pseudoClass, active);
	}

	@Override
	public void setStyle(String string) {
		superRoot.setStyle(string);

	}

	@Override
	public void setErrorLabelActive(boolean b) {
		errorLabel.setVisible(b);
	}

	Button getPlayButton() {
		return playButton;
	}

	Button getPauseButton() {
		return pauseButton;
	}

	Button getStopButton() {
		return stopButton;
	}

	Button getNewButton() {
		return newButton;
	}

	Button getSettingsButton() {
		return settingsButton;
	}

	public void setIndex(int indexReadable) {
		indexLabel.setText(String.valueOf(indexReadable));
	}

	public Property<Boolean> loopLabelVisibleProperty() {
		return loopLabel.visibleProperty();
	}

	public void setTriggerLabelActive(boolean hasTriggerItems) {
		triggerLabel.setVisible(hasTriggerItems);
	}

	public void setTime(String time) {
		if (time == null) {
			timeLabel.setText("");
		} else {
			timeLabel.setText(time);
		}
	}

	@Override
	public void addDefaultButton(Pad pad) {
		if (pad != null) {
			if (pad.getContent() != null) {
				if (pad.getContent() instanceof Pauseable) {
					buttonBox.getChildren().setAll(playButton, pauseButton, stopButton, newButton, settingsButton);
				} else {
					buttonBox.getChildren().setAll(playButton, stopButton, newButton, settingsButton);
				}
			} else {
				buttonBox.getChildren().setAll(newButton, settingsButton);
			}
		}
		infoBox.getChildren().setAll(indexLabel, loopLabel, triggerLabel, errorLabel, timeLabel);

		// Buttons unten Full Width
		buttonBox.prefWidthProperty().bind(superRoot.widthProperty());
		for (Node child : buttonBox.getChildren()) {
			if (child instanceof Region) {
				HBox.setHgrow(child, Priority.ALWAYS);
				((Region) child).setMaxWidth(Double.MAX_VALUE);
			}
		}

		// alle Labels in der InfoBox sollen die gleiche Höhe haben, damit die Icons auf gleicher höhe sind
		for (Node child : infoBox.getChildren()) {
			if (child instanceof Label) {
				((Label) child).setMaxHeight(Double.MAX_VALUE);
			}
		}
	}

	@Override
	public void applyStyleClasses() {
		Pad pad = getViewController().getPad();

		superRoot.getStyleClass().addAll("pad", "pad" + pad.getIndex());

		indexLabel.getStyleClass().addAll("pad-index", "pad" + pad.getIndex() + "-index", "pad-info", "pad" + pad.getIndex() + "-info");
		timeLabel.getStyleClass().addAll("pad-time", "pad" + pad.getIndex() + "-time", "pad-info", "pad" + pad.getIndex() + "-info");
		loopLabel.getGraphic().getStyleClass().addAll("pad-icon", "pad" + pad.getIndex() + "-icon");
		triggerLabel.getGraphic().getStyleClass().addAll("pad-icon", "pad" + pad.getIndex() + "-icon");
		errorLabel.getGraphic().getStyleClass().addAll("pad-icon", "pad" + pad.getIndex() + "-icon");

		preview.getChildren().forEach(i -> i.getStyleClass().addAll("pad-title", "pad" + pad.getIndex() + "-title"));

		playBar.getStyleClass().addAll("pad-playbar", "pad" + pad.getIndex() + "-playbar");

		playButton.getStyleClass().addAll("pad-button", "pad" + pad.getIndex() + "-button");
		pauseButton.getStyleClass().addAll("pad-button", "pad" + pad.getIndex() + "-button");
		stopButton.getStyleClass().addAll("pad-button", "pad" + pad.getIndex() + "-button");
		newButton.getStyleClass().addAll("pad-button", "pad" + pad.getIndex() + "-button");
		settingsButton.getStyleClass().addAll("pad-button", "pad" + pad.getIndex() + "-button");

		playButton.getGraphic().getStyleClass().addAll("pad-icon", "pad" + pad.getIndex() + "-icon");
		pauseButton.getGraphic().getStyleClass().addAll("pad-icon", "pad" + pad.getIndex() + "-icon");
		stopButton.getGraphic().getStyleClass().addAll("pad-icon", "pad" + pad.getIndex() + "-icon");
		newButton.getGraphic().getStyleClass().addAll("pad-icon", "pad" + pad.getIndex() + "-icon");
		settingsButton.getGraphic().getStyleClass().addAll("pad-icon", "pad" + pad.getIndex() + "-icon");

		buttonBox.getStyleClass().add("pad-button-box");
		root.getStyleClass().add("pad-root");
	}

	@Override
	public void removeStyleClasses() {
		Pad pad = getViewController().getPad();

		superRoot.getStyleClass().removeAll("pad", "pad" + pad.getIndex());

		indexLabel.getStyleClass().removeAll("pad-index", "pad" + pad.getIndex() + "-index", "pad-info", "pad" + pad.getIndex() + "-info");
		timeLabel.getStyleClass().removeAll("pad-time", "pad" + pad.getIndex() + "-time", "pad-info", "pad" + pad.getIndex() + "-info");
		loopLabel.getGraphic().getStyleClass().removeAll("pad-icon", "pad" + pad.getIndex() + "-icon");
		triggerLabel.getGraphic().getStyleClass().removeAll("pad-icon", "pad" + pad.getIndex() + "-icon");
		errorLabel.getGraphic().getStyleClass().removeAll("pad-icon", "pad" + pad.getIndex() + "-icon");

		preview.getChildren().forEach(i -> i.getStyleClass().removeAll("pad-title", "pad" + pad.getIndex() + "-title"));

		playBar.getStyleClass().removeAll("pad-playbar", "pad" + pad.getIndex() + "-playbar");

		playButton.getStyleClass().removeAll("pad-button", "pad" + pad.getIndex() + "-button");
		pauseButton.getStyleClass().removeAll("pad-button", "pad" + pad.getIndex() + "-button");
		stopButton.getStyleClass().removeAll("pad-button", "pad" + pad.getIndex() + "-button");
		newButton.getStyleClass().removeAll("pad-button", "pad" + pad.getIndex() + "-button");
		settingsButton.getStyleClass().removeAll("pad-button", "pad" + pad.getIndex() + "-button");

		playButton.getGraphic().getStyleClass().removeAll("pad-icon", "pad" + pad.getIndex() + "-icon");
		pauseButton.getGraphic().getStyleClass().removeAll("pad-icon", "pad" + pad.getIndex() + "-icon");
		stopButton.getGraphic().getStyleClass().removeAll("pad-icon", "pad" + pad.getIndex() + "-icon");
		newButton.getGraphic().getStyleClass().removeAll("pad-icon", "pad" + pad.getIndex() + "-icon");
		settingsButton.getGraphic().getStyleClass().removeAll("pad-icon", "pad" + pad.getIndex() + "-icon");

		buttonBox.getStyleClass().add("pad-button-box");
		root.getStyleClass().add("pad-root");
	}

	public void clearIndex() {
		indexLabel.setText("");
	}

	public void clearTime() {
		timeLabel.setText("");
	}

	public void clearPreviewContent() {
		if (previewContent != null) {
			previewContent.deinit();
		}
		setContentView(null);
	}

	@Override
	public void setPlaybarVisible(boolean visible) {
		playBar.setVisible(visible);
	}

	@Override
	public void setPlayBarProgress(double value) {
		playBar.setProgress(value);
	}
}
