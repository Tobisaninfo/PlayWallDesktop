package de.tobias.playpad.layout.desktop.pad;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.layout.desktop.DesktopMainLayoutFactory;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.ContentFactory;
import de.tobias.playpad.pad.conntent.PadContentRegistry;
import de.tobias.playpad.pad.conntent.play.Pauseable;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.pad.view.IPadView;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.view.EmptyPadView;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.ui.scene.BusyView;
import de.tobias.utils.ui.translation.PulseTranslation;
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

public class DesktopPadView implements IPadView {

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
	
	public DesktopPadView(DesktopMainLayoutFactory connect) {
		controller = new DesktopPadViewController(this, connect);
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
		superRoot.setUserData(pad);
		
		if (previewContent != null) {
			previewContent.deinit();
		}

		if (pad != null) {
			PadContent content = pad.getContent();
			if (content != null) {
				try {
					PadContentRegistry registry = PlayPadPlugin.getRegistryCollection().getPadContents();
					ContentFactory connect = registry.getFactory(content.getType());

					previewContent = connect.getPadContentPreview(pad, preview);
					Node node = previewContent.getNode();

					node.getStyleClass().addAll("pad-title", "pad" + pad.getPadIndex() + "-title");

					// Copy Pseudoclasses
					for (PseudoClass pseudoClass : superRoot.getPseudoClassStates()) {
						node.pseudoClassStateChanged(pseudoClass, true);
					}

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
			view.getStyleClass().addAll("pad-title", "pad" + pad.getPadIndex() + "-title");
		} else {
			view.getStyleClass().addAll("pad-title");
		}
		preview.getChildren().setAll(view);
	}

	@Override
	public IPadViewController getViewController() {
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
	public void addDefaultElements(Pad pad) {
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
	public void applyStyleClasses(PadIndex index) {
		superRoot.getStyleClass().addAll("pad", "pad" + index);

		indexLabel.getStyleClass().addAll("pad-index", "pad" + index + "-index", "pad-info", "pad" + index + "-info");
		timeLabel.getStyleClass().addAll("pad-time", "pad" + index + "-time", "pad-info", "pad" + index + "-info");
		loopLabel.getGraphic().getStyleClass().addAll("pad-icon", "pad" + index + "-icon");
		triggerLabel.getGraphic().getStyleClass().addAll("pad-icon", "pad" + index + "-icon");
		errorLabel.getGraphic().getStyleClass().addAll("pad-icon", "pad" + index + "-icon");

		preview.getChildren().forEach(i -> i.getStyleClass().addAll("pad-title", "pad" + index + "-title"));

		playBar.getStyleClass().addAll("pad-playbar", "pad" + index + "-playbar");

		playButton.getStyleClass().addAll("pad-button", "pad" + index + "-button");
		pauseButton.getStyleClass().addAll("pad-button", "pad" + index + "-button");
		stopButton.getStyleClass().addAll("pad-button", "pad" + index + "-button");
		newButton.getStyleClass().addAll("pad-button", "pad" + index + "-button");
		settingsButton.getStyleClass().addAll("pad-button", "pad" + index + "-button");

		playButton.getGraphic().getStyleClass().addAll("pad-icon", "pad" + index + "-icon");
		pauseButton.getGraphic().getStyleClass().addAll("pad-icon", "pad" + index + "-icon");
		stopButton.getGraphic().getStyleClass().addAll("pad-icon", "pad" + index + "-icon");
		newButton.getGraphic().getStyleClass().addAll("pad-icon", "pad" + index + "-icon");
		settingsButton.getGraphic().getStyleClass().addAll("pad-icon", "pad" + index + "-icon");

		buttonBox.getStyleClass().add("pad-button-box");
		root.getStyleClass().add("pad-root");
	}

	@Override
	public void removeStyleClasses() {
		Pad pad = getViewController().getPad();
		PadIndex index = pad.getPadIndex();

		superRoot.getStyleClass().removeAll("pad", "pad" + index);

		indexLabel.getStyleClass().removeAll("pad-index", "pad" + index + "-index", "pad-info", "pad" + index + "-info");
		timeLabel.getStyleClass().removeAll("pad-time", "pad" + index + "-time", "pad-info", "pad" + index + "-info");
		loopLabel.getGraphic().getStyleClass().removeAll("pad-icon", "pad" + index + "-icon");
		triggerLabel.getGraphic().getStyleClass().removeAll("pad-icon", "pad" + index + "-icon");
		errorLabel.getGraphic().getStyleClass().removeAll("pad-icon", "pad" + index + "-icon");

		preview.getChildren().forEach(i -> i.getStyleClass().removeAll("pad-title", "pad" + index + "-title"));

		playBar.getStyleClass().removeAll("pad-playbar", "pad" + index + "-playbar");

		playButton.getStyleClass().removeAll("pad-button", "pad" + index + "-button");
		pauseButton.getStyleClass().removeAll("pad-button", "pad" + index + "-button");
		stopButton.getStyleClass().removeAll("pad-button", "pad" + index + "-button");
		newButton.getStyleClass().removeAll("pad-button", "pad" + index + "-button");
		settingsButton.getStyleClass().removeAll("pad-button", "pad" + index + "-button");

		playButton.getGraphic().getStyleClass().removeAll("pad-icon", "pad" + index + "-icon");
		pauseButton.getGraphic().getStyleClass().removeAll("pad-icon", "pad" + index + "-icon");
		stopButton.getGraphic().getStyleClass().removeAll("pad-icon", "pad" + index + "-icon");
		newButton.getGraphic().getStyleClass().removeAll("pad-icon", "pad" + index + "-icon");
		settingsButton.getGraphic().getStyleClass().removeAll("pad-icon", "pad" + index + "-icon");

		buttonBox.getStyleClass().add("pad-button-box");
		root.getStyleClass().add("pad-root");
	}

	@Override
	public void highlightView(int milliSecounds) {
		// FadeableColor stopColor = new FadeableColor(ModernColor.GRAY1.getColorHi(), ModernColor.GRAY1.getColorLow());
		// FadeableColor playColor = new FadeableColor(ModernColor.BLUE1.getColorHi(), ModernColor.BLUE1.getColorLow());
		//
		// ModernDesignAnimator.animateWarn(controller, stopColor, playColor, Duration.seconds(3));
		// Pad pad = controller.getPad();
		//
		// Warning warning = pad.getWarning();
		//
		// if (pad.isCustomLayout()) {
		// pad.getLayout().handleWarning(controller, warning, Profile.currentProfile().currentLayout());
		// } else {
		// Profile.currentProfile().currentLayout().handleWarning(controller, warning);
		// }

		PulseTranslation pulseTranslation = new PulseTranslation(superRoot, null, 0.1);
		pulseTranslation.play();
	}

	public void clearIndexLabel() {
		indexLabel.setText("");
	}

	public void clearTimeLabel() {
		timeLabel.setText("");
	}

	public void clearPreviewContentView() {
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
