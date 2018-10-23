package de.tobias.playpad.viewcontroller.cell;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.tobias.playpad.viewcontroller.dialog.PathMatchDialog;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * Created by tobias on 24.03.17.
 */
public class PathMatchActionCell extends TableCell<PathMatchDialog.TempMediaPath, PathMatchDialog.TempMediaPath> implements EventHandler<MouseEvent> {

	private PathMatchDialog parentDialog;
	private PathMatchDialog.TempMediaPath currentItem;

	private FontIcon folderIcon;

	private FontIcon playIcon;
	private FontIcon stopIcon;
	private Button playButton;
	private HBox hbox;

	public PathMatchActionCell(PathMatchDialog parentDialog) {
		this.parentDialog = parentDialog;

		folderIcon = new FontIcon(FontAwesomeType.FOLDER_OPEN);

		playIcon = new FontIcon(FontAwesomeType.PLAY);
		stopIcon = new FontIcon(FontAwesomeType.STOP);

		playButton = new Button("", playIcon);

		folderIcon.setOnMouseClicked(this);
		playButton.setOnMouseClicked(this);

		folderIcon.getStyleClass().add("fonticon-notfound");
		playIcon.getStyleClass().add("fonticon-notfound");
		stopIcon.getStyleClass().add("fonticon-notfound");

		playButton.getStyleClass().clear();
	}

	@Override
	protected void updateItem(PathMatchDialog.TempMediaPath item, boolean empty) {
		super.updateItem(item, empty);
		currentItem = item;

		if (!empty) {
			hbox = new HBox(14, folderIcon, playButton);
			hbox.setAlignment(Pos.CENTER);
			setGraphic(hbox);
		} else {
			setGraphic(null);
		}
	}

	@Override
	public void handle(MouseEvent event) {
		if (currentItem != null) {
			if (event.getSource() == folderIcon) {
				parentDialog.showFileChooser(currentItem);
			} else if (event.getSource() == playButton) {
				if (currentItem.isMatched()) {
					if (currentItem.getPreviewPlayer() == null) { // PLAY
						MediaPlayer player = new MediaPlayer(new Media(currentItem.getLocalPath().toUri().toString()));
						player.play();
						player.setOnReady(player::play);
						player.setOnEndOfMedia(() -> {
							playButton.setGraphic(playIcon);
							currentItem.setPreviewPlayer(null);
						});
						currentItem.setPreviewPlayer(player);
						playButton.setGraphic(stopIcon);
					} else { // STOP
						MediaPlayer player = currentItem.getPreviewPlayer();
						player.stop();
						player.dispose();
						currentItem.setPreviewPlayer(null);
						playButton.setGraphic(playIcon);
					}
				}
			}
		}
	}
}
