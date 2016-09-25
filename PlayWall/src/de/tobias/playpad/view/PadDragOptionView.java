package de.tobias.playpad.view;

import java.util.Collection;

import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.pad.drag.PadDragMode;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Transition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class PadDragOptionView {

	private HBox optionPane;
	private Pane parent;

	private Transition inTransition;
	private Transition outTransition;

	public PadDragOptionView(Pane pane) {
		parent = pane;

		optionPane = new HBox();
		optionPane.prefWidthProperty().bind(parent.widthProperty());
		optionPane.prefHeightProperty().bind(parent.heightProperty());
		optionPane.setBackground(new Background(new BackgroundFill(new Color(0.2, 0.2, 0.2, 0.8), new CornerRadii(10), new Insets(0))));
		optionPane.setAlignment(Pos.CENTER);
		optionPane.setPadding(new Insets(5));
		optionPane.setSpacing(5);

		inTransition = createTransition(true);
		outTransition = createTransition(false);

	}

	public Transition getInTransition() {
		return inTransition;
	}

	public void setInTransition(Transition inTransition) {
		this.inTransition = inTransition;
	}

	public Transition getOutTransition() {
		return outTransition;
	}

	public void setOutTransition(Transition outTransition) {
		this.outTransition = outTransition;
	}

	private Transition createTransition(boolean in) {
		FadeTransition fadeTransition = new FadeTransition();
		fadeTransition.setNode(optionPane);

		ScaleTransition scaleTransition = new ScaleTransition();
		scaleTransition.setNode(optionPane);

		if (in) {
			fadeTransition.setFromValue(0);
			fadeTransition.setToValue(1);

			scaleTransition.setFromX(1.3);
			scaleTransition.setFromY(1.3);
			scaleTransition.setToX(1);
			scaleTransition.setToY(1);
		} else {
			fadeTransition.setFromValue(1);
			fadeTransition.setToValue(0);

			scaleTransition.setFromX(1);
			scaleTransition.setFromY(1);
			scaleTransition.setToX(1.3);
			scaleTransition.setToY(1.3);
		}

		ParallelTransition parallelTransition = new ParallelTransition(fadeTransition, scaleTransition);

		parallelTransition.setOnFinished((e) ->
		{
			if (!in)
				parent.getChildren().remove(optionPane);
		});
		return parallelTransition;
	}

	private PadDragMode selectedConnect;

	public void showDropOptions(Collection<PadDragMode> options) {
		if (!parent.getChildren().contains(optionPane)) {
			selectedConnect = null;

			parent.getChildren().add(optionPane);
			optionPane.getChildren().clear();

			for (PadDragMode connect : options.stream().sorted().toArray(value -> new PadDragMode[value])) {
				Label label = new Label();
				label.getStyleClass().add("dnd-file-option");

				// Text nur wenn 2 oder weniger Optionen sind, weil zu wenig Platz
				if (options.size() >= 2) {
					label.textProperty().bind(connect.displayProperty());
				}

				Node graphics = connect.getGraphics();
				if (graphics != null) {
					graphics.setStyle("-fx-text-fill: white;");
					label.setGraphic(graphics);
				}
				label.setWrapText(true);

				label.setOnDragOver(e ->
				{
					label.pseudoClassStateChanged(PseudoClasses.HOVER_CLASS, true);
					selectedConnect = connect;
				});
				label.setOnDragExited(e ->
				{
					label.pseudoClassStateChanged(PseudoClasses.HOVER_CLASS, false);
					selectedConnect = null;
				});

				label.setUserData(connect);

				label.setAlignment(Pos.CENTER);
				label.setTextAlignment(TextAlignment.CENTER);
				label.setContentDisplay(ContentDisplay.TOP);

				label.maxWidthProperty().bind(optionPane.widthProperty().divide(options.size()).subtract(12.5));
				label.setMaxHeight(Double.MAX_VALUE);
				HBox.setHgrow(label, Priority.ALWAYS);

				optionPane.getChildren().add(label);
			}

			inTransition.play();
		}
	}

	public PadDragMode getSelectedPadDragMode() {
		return selectedConnect;
	}

	public void hide() {
		outTransition.play();
	}
}
