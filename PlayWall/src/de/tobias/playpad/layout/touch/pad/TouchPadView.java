package de.tobias.playpad.layout.touch.pad;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.DesignColorAssociator;
import de.tobias.playpad.design.GlobalDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.pad.view.IPadView;
import de.tobias.playpad.pad.viewcontroller.IPadViewController;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.project.page.PadIndex;
import de.tobias.playpad.registry.NoSuchComponentException;
import de.tobias.playpad.util.ColorUtils;
import de.tobias.playpad.view.EmptyPadView;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.ui.scene.BusyView;
import de.tobias.utils.util.OS;
import de.tobias.utils.util.win.User32X;
import javafx.beans.property.Property;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class TouchPadView implements IPadView {

	private Label indexLabel;
	private Label loopLabel;
	private Label triggerLabel;
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

	private transient TouchPadViewController controller; // Reference to its controller

	public TouchPadView() {
		controller = new TouchPadViewController(this);
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

		// Not Found Label
		notFoundLabel = new FontIcon(FontAwesomeType.EXCLAMATION_TRIANGLE);
		notFoundLabel.getStyleClass().add("pad-notfound");
		notFoundLabel.setOpacity(0.5);
		notFoundLabel.setSize(50);
		notFoundLabel.setMouseTransparent(true);

		notFoundLabel.setVisible(false);

		root.getChildren().addAll(infoBox, preview, playBar);
		superRoot.getChildren().addAll(root, notFoundLabel);

		if (OS.isWindows() && User32X.isTouchAvailable()) {
			superRoot.setOnTouchPressed(controller);
			playBar.setOnTouchPressed(controller);
		} else {
			superRoot.setOnMouseClicked(controller);
			playBar.setOnMouseClicked(controller);
		}
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
					PadContentFactory connect = PlayPadPlugin.getRegistryCollection().getPadContents().getFactory(content.getType());
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

	ProgressBar getPlayBar() {
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
		infoBox.getChildren().setAll(indexLabel, loopLabel, triggerLabel, errorLabel, timeLabel);

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

		root.getStyleClass().add("pad-root");
	}

	@Override
	public void removeStyleClasses() {
		superRoot.getStyleClass().removeIf( c -> c.startsWith("pad"));

		indexLabel.getStyleClass().removeIf( c -> c.startsWith("pad"));
		timeLabel.getStyleClass().removeIf( c -> c.startsWith("pad"));
		loopLabel.getGraphic().getStyleClass().removeIf( c -> c.startsWith("pad"));
		triggerLabel.getGraphic().getStyleClass().removeIf( c -> c.startsWith("pad"));
		errorLabel.getGraphic().getStyleClass().removeIf( c -> c.startsWith("pad"));

		preview.getChildren().forEach(i -> i.getStyleClass().removeIf( c -> c.startsWith("pad")));

		playBar.getStyleClass().removeIf( c -> c.startsWith("pad"));

		root.getStyleClass().remove("pad-root");
	}

	@Override
	public void highlightView(int milliSecounds) {
		
	}
	
	void clearIndex() {
		indexLabel.setText("");
	}

	void clearTime() {
		timeLabel.setText("");
	}

	void clearPreviewContent() {
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

	@Override
	public void showNotFoundIcon(Pad pad, boolean show) {
		if (show) {
			DesignColorAssociator associator = null;
			if (pad.getPadSettings().isCustomDesign()) {
				CartDesign design = pad.getPadSettings().getDesign();
				if (design instanceof DesignColorAssociator) {
					associator = (DesignColorAssociator) design;
				}
			} else {
				GlobalDesign design = Profile.currentProfile().currentLayout();
				if (design instanceof DesignColorAssociator) {
					associator = (DesignColorAssociator) design;
				}
			}

			if (associator != null) {
				Color color = associator.getAssociatedStandardColor();
				notFoundLabel.setColor(ColorUtils.getWarningSignColor(color));
			} else {
				notFoundLabel.setColor(Color.RED);
			}
		}
		notFoundLabel.setVisible(show);
		root.setOpacity(show ? 0.5 : 1.0);
	}
}
