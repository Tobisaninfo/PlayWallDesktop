package de.tobias.playpad.pad.content;

import de.tobias.playpad.Strings;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.conntent.PadContent;
import de.tobias.playpad.pad.conntent.PadContentConnect;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.viewcontroller.option.AudioTabViewController;
import de.tobias.playpad.viewcontroller.option.SettingsTabViewController;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class AudioContentConnect extends PadContentConnect {

	public static final String TYPE = "audio";
	public static final String[] FILE_EXTENSION = { "*.mp3", "*.wav" };

	private FontIcon icon;

	public AudioContentConnect() {
		icon = new FontIcon(FontAwesomeType.MUSIC);
		icon.setSize(30);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public PadContent newInstance(Pad pad) {
		return new AudioContent(pad);
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
	public SettingsTabViewController getSettingsTabViewController(boolean activePlayer) {
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
		public void unconnect() {
			nameLabel.textProperty().unbind();
		}
	}

	// UI - DnD
	@Override
	public StringProperty displayProperty() {
		return new SimpleStringProperty(Localization.getString(Strings.Content_Audio_Name));
	}

	@Override
	public Node getGraphics() {
		return icon;
	}

}
