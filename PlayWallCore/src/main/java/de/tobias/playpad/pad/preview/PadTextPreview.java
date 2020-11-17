package de.tobias.playpad.pad.preview;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.view.IPadContentView;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class PadTextPreview implements IPadContentView {

	private final Label nameLabel;

	public PadTextPreview(Pad pad, Pane parentNode) {
		this.nameLabel = new Label();
		this.nameLabel.textProperty().bind(pad.nameProperty());

		this.nameLabel.setWrapText(true);
		this.nameLabel.setAlignment(Pos.CENTER);
		this.nameLabel.setTextAlignment(TextAlignment.CENTER);

		this.nameLabel.prefWidthProperty().bind(parentNode.widthProperty());
		this.nameLabel.setMaxHeight(Double.MAX_VALUE);

		VBox.setVgrow(nameLabel, Priority.ALWAYS);
	}

	@Override
	public Node getNode() {
		return nameLabel;
	}

	@Override
	public void deInit() {
		nameLabel.textProperty().unbind();
	}
}
