package de.tobias.playpad.plugin.media.image;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.PadContent;
import de.tobias.playpad.pad.content.PadContentFactory;
import de.tobias.playpad.pad.view.IPadContentView;
import de.tobias.playpad.viewcontroller.PadSettingsTabViewController;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

public class ImagePadContentFactory extends PadContentFactory {

	private static final String[] FILE_EXTENSION = {"*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"};

	public ImagePadContentFactory(String type) {
		super(type);
	}

	@Override
	public PadContent newInstance(Pad pad) {
		return new ImageContent(getType(), pad);
	}

	@Override
	public IPadContentView getPadContentPreview(Pad pad, Pane parentNode) {
		return new ImageContentView(pad, parentNode);
	}

	@Override
	public PadSettingsTabViewController getSettingsViewController(Pad pad) {
		return null;
	}

	@Override
	public String[] getSupportedTypes() {
		return FILE_EXTENSION;
	}

	class ImageContentView implements IPadContentView {

		private StackPane stackPane;
		private Label nameLabel;
		private Label imageLabel;
		private Pad pad;

		ImageContentView(Pad pad, Pane parentNode) {
			this.pad = pad;
			nameLabel = new Label();
			nameLabel.textProperty().bind(pad.nameProperty());

			nameLabel.setWrapText(true);
			nameLabel.setAlignment(Pos.CENTER);
			nameLabel.setTextAlignment(TextAlignment.CENTER);

			imageLabel = new Label();
			imageLabel.setMaxHeight(Double.MAX_VALUE);
			imageLabel.setMaxWidth(Double.MAX_VALUE);
			setImage();

			stackPane = new StackPane(imageLabel, nameLabel);
			stackPane.prefWidthProperty().bind(parentNode.widthProperty());
			stackPane.setMaxHeight(Double.MAX_VALUE);
			VBox.setVgrow(stackPane, Priority.ALWAYS);

			// Leitet alle StyleClasses von Parent Object an das NameLabel weiter
			stackPane.getStyleClass().addListener((ListChangeListener<String>) c -> {
				while (c.next()) {
					for (String remitem : c.getRemoved()) {
						nameLabel.getStyleClass().remove(remitem);

					}
					for (String additem : c.getAddedSubList()) {
						nameLabel.getStyleClass().add(additem);
					}
				}
			});
			// Leitet alle PseudoClassStates von Parent Object an das NameLabel weiter
			stackPane.getPseudoClassStates().addListener((SetChangeListener<PseudoClass>) c -> {
				nameLabel.pseudoClassStateChanged(c.getElementRemoved(), false);
				nameLabel.pseudoClassStateChanged(c.getElementAdded(), true);
			});
		}

		@Override
		public Node getNode() {
			return stackPane;
		}

		@Override
		public void deinit() {
			nameLabel.textProperty().unbind();
		}

		void setImage() {

			if (pad.getPath() != null)
				imageLabel.setStyle("-fx-background-image: url(\"" + pad.getPath().toUri().toString()
						+ "\"); -fx-background-size: contain; -fx-background-repeat: no-repeat; -fx-background-position: center; -fx-opacity: 0.3;");
		}
	}
}
