package de.tobias.playpad.mediaplugin.video;

import de.tobias.playpad.mediaplugin.main.impl.MediaPluginImpl;
import de.tobias.playpad.mediaplugin.main.impl.MediaSettingsTabViewController;
import de.tobias.playpad.mediaplugin.main.impl.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import de.tobias.playpad.viewcontroller.SettingsTabViewController;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class VideoContentConntect extends PadContentConnect {

	public static final String TYPE = "video";
	public static final String[] FILE_EXTENSION = { "*.mp4", "*.mov" };

	private FontIcon icon;

	public VideoContentConntect() {
		icon = new FontIcon(FontAwesomeType.FILM);
		icon.setSize(30);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public PadContent newInstance(Pad pad) {
		return new VideoContent(pad);
	}

	@Override
	public IPadContentView getPadContentPreview(Pad pad, Pane parentNode) {
		return new VideoContentView(pad, parentNode);
	}

	@Override
	public SettingsTabViewController getSettingsTabViewController(boolean activePlayer) {
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

	private class VideoContentView implements IPadContentView {

		private Label nameLabel;

		public VideoContentView(Pad pad, Pane parentNode) {
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
		public void unconnect() {
			nameLabel.textProperty().unbind();
		}
	}

	@Override
	public StringProperty displayProperty() {
		return new SimpleStringProperty(MediaPluginImpl.getInstance().getBundle().getString(Strings.Content_Video_Name));
	}

	@Override
	public Node getGraphics() {
		return icon;
	}

}