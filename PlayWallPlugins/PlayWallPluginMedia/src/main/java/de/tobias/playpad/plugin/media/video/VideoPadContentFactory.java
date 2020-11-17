package de.tobias.playpad.plugin.media.video;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.plugin.media.main.impl.MediaPluginImpl;
import de.tobias.playpad.plugin.media.main.impl.MediaSettingsTabViewController;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.option.ProfileSettingsTabViewController;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class VideoPadContentFactory extends PadContentFactory {

	private static final String[] FILE_EXTENSION = {"*.mp4", "*.mov"};

	public VideoPadContentFactory(String type) {
		super(type);
	}

	@Override
	public PadContent newInstance(Pad pad) {
		return new VideoContent(getType(), pad);
	}

	@Override
	public IPadContentView getPadContentPreview(Pad pad, Pane parentNode) {
		return new VideoContentView(pad, parentNode);
	}

	@Override
	public ProfileSettingsTabViewController getSettingsTabViewController(boolean activePlayer) {
		MediaPluginImpl instance = MediaPluginImpl.getInstance();
		return new MediaSettingsTabViewController(instance.getCurrentSettings());
	}

	@Override
	public PadSettingsTabViewController getSettingsViewController(Pad pad) {
		return new VideoPadSettingsTabViewController();
	}

	@Override
	public String[] getSupportedTypes() {
		return FILE_EXTENSION;
	}

	private static class VideoContentView implements IPadContentView {

		private final Label nameLabel;

		VideoContentView(Pad pad, Pane parentNode) {
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
		public void deInit() {
			nameLabel.textProperty().unbind();
		}
	}
}
