package de.tobias.playpad.layout.desktop.pad;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.ui.animation.PulseTranslation;
import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.ui.scene.BusyView;
import de.thecodelabs.utils.util.ColorUtils;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.design.FeedbackDesignColorSuggester;
import de.tobias.playpad.design.modern.model.ModernCartDesign;
import de.tobias.playpad.layout.desktop.DesktopMainLayoutFactory;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.pad.content.PadContentRegistry;
import de.tobias.playpad.pad.content.Playlistable;
import de.tobias.playpad.pad.content.play.Pauseable;
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
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import static de.tobias.playpad.view.pad.PadStyleClasses.*;

public class DesktopPadView implements IPadView {

	private Label indexLabel;
	private Label loopLabel;
	private Label triggerLabel;
	private Label playlistLabel;
	private Label errorLabel;

	private HBox infoBox;
	private Label timeLabel;

	private HBox preview;
	private IPadContentView previewContent;

	private FontIcon notFoundLabel;

	private ProgressBar playBar;
	private Button playButton;
	private Button pauseButton;
	private Button nextButton;
	private Button stopButton;
	private Button newButton;
	private Button settingsButton;
	private HBox buttonBox;

	private StackPane superRoot;
	private VBox root;
	private BusyView busyView;

	private Label cueInLayer;

	// Reference to its controller
	private final transient DesktopPadViewController controller;

	public DesktopPadView(DesktopMainLayoutFactory connect) {
		controller = new DesktopPadViewController(this, connect);
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

		// Buttons
		playButton = new PadButton(new FontIcon(FontAwesomeType.PLAY), controller);
		pauseButton = new PadButton(new FontIcon(FontAwesomeType.PAUSE), controller);
		nextButton = new PadButton(new FontIcon(FontAwesomeType.STEP_FORWARD), controller);
		stopButton = new PadButton(new FontIcon(FontAwesomeType.STOP), controller);
		newButton = new PadButton(new FontIcon(FontAwesomeType.FOLDER_OPEN), controller);
		settingsButton = new PadButton(new FontIcon(FontAwesomeType.GEAR), controller);

		// Not Found Label
		notFoundLabel = new FontIcon(FontAwesomeType.EXCLAMATION_TRIANGLE);
		notFoundLabel.getStyleClass().clear();
		notFoundLabel.setOpacity(0.75);
		notFoundLabel.setSize(80);
		notFoundLabel.setMouseTransparent(true);
		notFoundLabel.setVisible(false);

		// Button HBOX
		buttonBox = new PadHBox(STYLE_CLASS_PAD_BUTTON_BOX);

		root.getChildren().addAll(infoBox, preview, playBar, buttonBox);
		superRoot.getChildren().addAll(cueInContainer, root, notFoundLabel);
	}

	@Override
	public IPadContentView getContentView() {
		return previewContent;
	}

	@Override
	public void setContentView(Pad pad) {
		superRoot.setUserData(pad);

		if (previewContent != null) {
			previewContent.deInit();
		}

		if (pad != null) {
			PadContent content = pad.getContent();
			if (content != null) {
				try {
					PadContentRegistry registry = PlayPadPlugin.getRegistries().getPadContents();
					PadContentFactory connect = registry.getFactory(content.getType());

					previewContent = connect.getPadContentPreview(pad, preview);
					Parent node = previewContent.getNode();

					// Copy Pseudo classes
					for (PseudoClass pseudoClass : superRoot.getPseudoClassStates()) {
						NodeWalker.getAllNodes(node).forEach(element -> element.pseudoClassStateChanged(pseudoClass, true));
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

	Button getPlayButton() {
		return playButton;
	}

	Button getPauseButton() {
		return pauseButton;
	}

	Button getNextButton() {
		return nextButton;
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
		if (pad != null) {
			if (pad.getContent() != null) {
				if (pad.getContent() instanceof Pauseable) {
					if (pad.getStatus() == PadStatus.PLAY) {
						if (pad.getContent() instanceof Playlistable) {
							buttonBox.getChildren().setAll(pauseButton, nextButton, stopButton, settingsButton);
						} else {
							buttonBox.getChildren().setAll(pauseButton, stopButton, settingsButton);
						}
					} else {
						buttonBox.getChildren().setAll(playButton, stopButton, settingsButton);
					}
				} else {
					buttonBox.getChildren().setAll(playButton, stopButton, settingsButton);
				}
			} else {
				buttonBox.getChildren().setAll(newButton, settingsButton);
			}
			applyStyleClasses(pad.getPadIndex());
		}
		infoBox.getChildren().setAll(indexLabel, loopLabel, triggerLabel, playlistLabel, errorLabel, timeLabel);

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

	void clearIndexLabel() {
		indexLabel.setText("");
	}

	void clearTimeLabel() {
		timeLabel.setText("");
	}

	void clearPreviewContentView() {
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
	public void setCueInProgress(double milliSeconds) {
		cueInLayer.setPrefWidth(root.getWidth() * milliSeconds);
	}

	@Override
	public void showNotFoundIcon(Pad pad, boolean show) {
		if (show) {
			FeedbackDesignColorSuggester globalDesign = Profile.currentProfile().getProfileSettings().getDesign();
			Color layoutStdColor = globalDesign.getDesignDefaultColor();

			if (pad != null) {
				final ModernCartDesign padDesign = pad.getPadSettings().getDesign();

				if (padDesign.isEnableCustomBackgroundColor()) {
					layoutStdColor = padDesign.getDesignDefaultColor();
				}
			}

			notFoundLabel.setColor(ColorUtils.getAppropriateTextColor(layoutStdColor));
		}
		notFoundLabel.setVisible(show);
		root.setOpacity(show ? 0.5 : 1.0);
	}
}
