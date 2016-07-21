package de.tobias.playpad.view;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.PadContentRegistry;
import de.tobias.playpad.pad.conntent.UnkownPadContentException;
import de.tobias.playpad.pad.conntent.play.Pauseable;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.pad.view.IPadViewController;
import de.tobias.playpad.viewcontroller.IPadView;
import de.tobias.playpad.viewcontroller.pad.PadViewController;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.ui.scene.BusyView;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PadView extends StackPane implements IPadView {

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

	private VBox root;
	private BusyView busyView;

	private transient PadViewController controller; // Reference to its controller

	public PadView(PadViewController controller) {
		this.controller = controller;

		root = new VBox();
		busyView = new BusyView(this);

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
		getChildren().addAll(root);
	}

	// wird über den Status listener gesteuert
	public void setErrorLabelActive(boolean active) {
		errorLabel.setVisible(active);
	}

	public void setTriggerLabelActive(boolean active) {
		triggerLabel.setVisible(active);
	}

	public Label getIndexLabel() {
		return indexLabel;
	}

	public Label getTimeLabel() {
		return timeLabel;
	}

	public ProgressBar getPlayBar() {
		return playBar;
	}

	public Button getPlayButton() {
		return playButton;
	}

	public Button getPauseButton() {
		return pauseButton;
	}

	public Button getStopButton() {
		return stopButton;
	}

	public Button getNewButton() {
		return newButton;
	}

	public Button getSettingsButton() {
		return settingsButton;
	}

	public HBox getButtonBox() {
		return buttonBox;
	}

	public HBox getInfoBox() {
		return infoBox;
	}

	public IPadViewController getController() {
		return controller;
	}

	public Label getLoopLabel() {
		return loopLabel;
	}

	public Label getErrorLabel() {
		return errorLabel;
	}

	public VBox getRoot() {
		return root;
	}

	public void setPreviewContent(Pad pad) {
		if (previewContent != null) {
			previewContent.unconnect();
		}

		if (pad != null) {
			PadContent content = pad.getContent();
			if (content != null) {
				try {
					previewContent = PadContentRegistry.getPadContentConnect(content.getType()).getPadContentPreview(pad, preview);
					Node node = previewContent.getNode();

					node.getStyleClass().addAll("pad-title", "pad" + pad.getIndex() + "-title");
					preview.getChildren().setAll(node);
					return;
				} catch (UnkownPadContentException e) {
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

	public void clearPreviewContent() {
		if (previewContent != null) {
			previewContent.unconnect();
		}
		setPreviewContent(null);
	}

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
		buttonBox.prefWidthProperty().bind(widthProperty());
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

	public void setBusy(boolean busy) {
		busyView.showProgress(busy);
	}

	public void pseudoClassState(PseudoClass pseudoClass, boolean active) {
		pseudoClassStateChanged(pseudoClass, active);
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

	public void addStyleClasses(Pad pad) {
		getStyleClass().addAll("pad", "pad" + pad.getIndex());

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

		getButtonBox().getStyleClass().add("pad-button-box");
		getRoot().getStyleClass().add("pad-root");
	}

	public void removeStyleClasses(Pad pad) {
		getStyleClass().removeAll("pad", "pad" + pad.getIndex());

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

		getButtonBox().getStyleClass().add("pad-button-box");
		getRoot().getStyleClass().add("pad-root");
	}

	public void showPlaybar(boolean b) {
		if (b) {
			if (!root.getChildren().contains(playBar))
				root.getChildren().add(2, playBar);
		} else {
			root.getChildren().remove(playBar);
		}
	}

	@Override
	public IPadContentView getPadContentView() {
		return previewContent;
	}
}
