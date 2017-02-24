package de.tobias.playpad.pad.content;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import de.tobias.playpad.viewcontroller.option.profile.AudioTabViewController;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class AudioPadContentFactory extends PadContentFactory {

	public static final String[] FILE_EXTENSION = { "*.mp3", "*.wav" };

	public AudioPadContentFactory(String type) {
		super(type);
	}

	@Override
	public PadContent newInstance(Pad pad) {
		return new AudioContent(getType(), pad);
	}

	@Override
	public String[] getSupportedTypes() {
		return FILE_EXTENSION;
	}

	@Override
	public IPadContentView getPadContentPreview(Pad pad, Pane parentNode) {
		if (pad.getContent() != null) {
			AudioContentView view = new AudioContentView(pad, parentNode);

			return view;
		} else {
			return null;
		}
	}

	@Override
	public ProfileSettingsTabViewController getSettingsTabViewController(boolean activePlayer) {
		return new AudioTabViewController(activePlayer);
	}

	private class AudioContentView implements IPadContentView {

		private Label nameLabel;

		public AudioContentView(Pad pad, Pane parentNode) {
			nameLabel = new Label();
			nameLabel.textProperty().bind(pad.nameProperty());

			nameLabel.setWrapText(true);
			nameLabel.setAlignment(Pos.CENTER);
			nameLabel.setTextAlignment(TextAlignment.CENTER);

			nameLabel.prefWidthProperty().bind(parentNode.widthProperty());
			nameLabel.setMaxHeight(Double.MAX_VALUE);
			VBox.setVgrow(nameLabel, Priority.ALWAYS);
		}

		@Override
		public Node getNode() {
			return nameLabel;
		}

		@Override
		public void deinit() {
			nameLabel.textProperty().unbind();
		}
	}
}
