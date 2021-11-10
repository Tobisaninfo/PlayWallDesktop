package de.tobias.playpad.layout.touch.pad;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.animation.PulseTranslation;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.scene.BusyView;
import de.thecodelabs.utils.util.ColorUtils;
import de.thecodelabs.utils.util.OS;
import de.thecodelabs.utils.util.win.User32X;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.design.FeedbackDesignColorSuggester;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.pad.view.IPadView;
import de.tobias.playpad.pad.viewcontroller.AbstractPadViewController;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.util.NodeWalker;
import de.tobias.playpad.view.EmptyPadView;
import de.tobias.playpad.view.PseudoClasses;
import de.tobias.playpad.view.pad.*;
import javafx.beans.property.Property;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import static de.tobias.playpad.view.pad.PadStyleClasses.*;

public class TouchPadView implements IPadView {

	private Label indexLabel;
	private Label loopLabel;
	private Label triggerLabel;
	private Label playlistLabel;
	private Label errorLabel;

	private HBox infoBox;
	private Label timeLabel;

	private FontIcon notFoundLabel;

	private HBox preview;
	private IPadContentView previewContent;

	private ProgressBar playBar;

	private StackPane superRoot;
	private VBox root;
	private BusyView busyView;

	private Label cueInLayer;

	private final transient TouchPadViewController controller; // Reference to its controller

	public TouchPadView() {
		controller = new TouchPadViewController(this);
		setupView();
	}

	private void setupView() {
		superRoot = new PadStackPane(STYLE_CLASS_PAD, STYLE_CLASS_PAD_INDEX);
		root = new PadVBox(STYLE_CLASS_PAD_BUTTON_ROOT);
		busyView = new BusyView(superRoot);

		cueInLayer = PadLabel.empty(STYLE_CLASS_PAD_CUE_IN, STYLE_CLASS_PAD_CUE_IN_INDEX);
		cueInLayer.prefHeightProperty().bind(root.heightProperty());
		VBox cueInContainer = new VBox(cueInLayer);

		indexLabel = PadLabel.empty(STYLE_CLASS_PAD_INFO, STYLE_CLASS_PAD_INFO_INDEX);
		timeLabel = PadLabel.empty(STYLE_CLASS_PAD_INFO, STYLE_CLASS_PAD_INFO_INDEX);

		loopLabel = new PadLabel(new FontIcon(FontAwesomeType.REPEAT));
		triggerLabel = new PadLabel(new FontIcon(FontAwesomeType.EXTERNAL_LINK));
		playlistLabel = PadLabel.empty(STYLE_CLASS_PAD_INFO, STYLE_CLASS_PAD_INFO_INDEX);
		errorLabel = new PadLabel(new FontIcon(FontAwesomeType.WARNING));

		infoBox = new PadHBox(5);

		preview = PadHBox.deepStyled(STYLE_CLASS_PAD_TITLE, STYLE_CLASS_PAD_TITLE_INDEX);
		HBox.setHgrow(preview, Priority.ALWAYS);
		VBox.setVgrow(preview, Priority.ALWAYS);

		HBox.setHgrow(timeLabel, Priority.ALWAYS);
		timeLabel.setMaxWidth(Double.MAX_VALUE);
		timeLabel.setAlignment(Pos.CENTER_RIGHT);

		playBar = new PadProgressBar(0, STYLE_CLASS_PAD_PLAYBAR, STYLE_CLASS_PAD_PLAYBAR_INDEX);
		playBar.prefWidthProperty().bind(root.widthProperty());

		// Not Found Label
		notFoundLabel = new FontIcon(FontAwesomeType.EXCLAMATION_TRIANGLE);
		notFoundLabel.getStyleClass().clear();
		notFoundLabel.setOpacity(0.5);
		notFoundLabel.setSize(50);
		notFoundLabel.setMouseTransparent(true);
		notFoundLabel.setVisible(false);

		root.getChildren().addAll(infoBox, preview, playBar);
		superRoot.getChildren().addAll(cueInContainer, root, notFoundLabel);

		if (OS.isWindows() && User32X.isTouchAvailable()) {
			superRoot.setOnTouchPressed(controller);
		} else {
			superRoot.setOnMouseClicked(controller);
		}
		playBar.setMouseTransparent(true);
	}

	@Override
	public IPadContentView getContentView() {
		return previewContent;
	}

	@Override
	public void setContentView(Pad pad) {
		if (previewContent != null) {
			previewContent.deInit();
		}

		if (pad != null) {
			PadContent content = pad.getContent();
			if (content != null) {
				try {
					PadContentFactory connect = PlayPadPlugin.getRegistries().getPadContents().getFactory(content.getType());
					previewContent = connect.getPadContentPreview(pad, preview);
					Node node = previewContent.getNode();

					// Copy Pseudoclasses
					for (PseudoClass pseudoClass : superRoot.getPseudoClassStates()) {
						node.pseudoClassStateChanged(pseudoClass, true);
					}

					preview.getChildren().setAll(node);
					return;
				} catch (NoSuchComponentException e) {
					Logger.error(e);
				}
			}
		}
		EmptyPadView view = new EmptyPadView(preview);
		preview.getChildren().setAll(view);
	}

	@Override
	public AbstractPadViewController getViewController() {
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

	ProgressBar getPlayBar() {
		return playBar;
	}

	@Override
	public void pseudoClassState(PseudoClass pseudoClass, boolean active) {
		NodeWalker.getAllNodes(getRootNode())
				.forEach(node -> node.pseudoClassStateChanged(pseudoClass, active));
	}

	@Override
	public void setStyle(String string) {
		superRoot.setStyle(string);
	}

	@Override
	public void setErrorLabelActive(boolean b) {
		errorLabel.setVisible(b);
	}

	public void setIndex(int indexReadable) {
		indexLabel.setText(String.valueOf(indexReadable));
	}

	Property<Boolean> loopLabelVisibleProperty() {
		return loopLabel.visibleProperty();
	}

	void setTriggerLabelActive(boolean hasTriggerItems) {
		triggerLabel.setVisible(hasTriggerItems);
	}

	void setTime(String time) {
		if (time == null) {
			timeLabel.setText("");
		} else {
			timeLabel.setText(time);
		}
	}

	@Override
	public void addDefaultElements(Pad pad) {
		infoBox.getChildren().setAll(indexLabel, loopLabel, triggerLabel, playlistLabel, errorLabel, timeLabel);

		// alle Labels in der InfoBox sollen die gleiche Höhe haben, damit die Icons auf gleicher höhe sind
		for (Node child : infoBox.getChildren()) {
			if (child instanceof Label) {
				((Label) child).setMaxHeight(Double.MAX_VALUE);
			}
		}
	}

	@Override
	public void applyStyleClasses(PadIndex index) {
		NodeWalker.getAllNodes(getRootNode())
				.stream()
				.filter(node -> node instanceof PadIndexable)
				.forEach(node -> ((PadIndexable) node).setIndex(index));
	}

	@Override
	public void removeStyleClasses() {
		NodeWalker.getAllNodes(getRootNode())
				.stream()
				.filter(node -> node instanceof PadIndexable)
				.forEach(node -> ((PadIndexable) node).setIndex(null));
	}

	@Override
	public void highlightView(int milliSeconds) {
		PulseTranslation pulseTranslation = new PulseTranslation(superRoot, null, 0.1);
		pulseTranslation.play();
	}

	void clearIndex() {
		indexLabel.setText("");
	}

	void clearTime() {
		timeLabel.setText("");
	}

	void clearPreviewContent() {
		if (previewContent != null) {
			previewContent.deInit();
		}
		setContentView(null);
	}

	public Label getPlaylistLabel() {
		return playlistLabel;
	}

	@Override
	public void setPlaybarVisible(boolean visible) {
		playBar.setVisible(visible);
	}

	@Override
	public void setPlayBarProgress(double value) {
		playBar.setProgress(value);
	}

	@Override
	public void setCueInProgress(double value) {
		cueInLayer.setPrefWidth(root.getWidth() * value);
	}

	@Override
	public void showNotFoundIcon(Pad pad, boolean show) {
		if (show) {
			FeedbackDesignColorSuggester associator = null;
			if (pad.getPadSettings().isCustomDesign()) {
				associator = pad.getPadSettings().getDesign();
			} else {
				associator = Profile.currentProfile().getProfileSettings().getDesign();
			}

			if (associator != null) {
				Color color = associator.getDesignDefaultColor();
				notFoundLabel.setColor(ColorUtils.getAppropriateTextColor(color));
			} else {
				notFoundLabel.setColor(Color.RED);
			}
		}
		notFoundLabel.setVisible(show);
		root.setOpacity(show ? 0.5 : 1.0);
	}
}
