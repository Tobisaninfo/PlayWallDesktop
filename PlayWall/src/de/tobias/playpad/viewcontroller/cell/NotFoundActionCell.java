package de.tobias.playpad.viewcontroller.cell;

import de.tobias.playpad.viewcontroller.dialog.NotFoundDialog;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

/**
 * Created by tobias on 24.03.17.
 */
public class NotFoundActionCell extends TableCell<NotFoundDialog.TempMediaPath, NotFoundDialog.TempMediaPath> implements EventHandler<MouseEvent> {

	private NotFoundDialog parentDialog;
	private NotFoundDialog.TempMediaPath currentItem;

	private FontIcon folderIcon;
	private FontIcon playIcon;
	private HBox hbox;

	public NotFoundActionCell(NotFoundDialog parentDialog) {
		this.parentDialog = parentDialog;

		folderIcon = new FontIcon(FontAwesomeType.FOLDER_OPEN);
		playIcon = new FontIcon(FontAwesomeType.PLAY);

		folderIcon.setOnMouseClicked(this);
		playIcon.setOnMouseClicked(this);

		folderIcon.getStyleClass().add("fonticon-notfound");
		playIcon.getStyleClass().add("fonticon-notfound");
	}

	@Override
	protected void updateItem(NotFoundDialog.TempMediaPath item, boolean empty) {
		super.updateItem(item, empty);
		currentItem = item;

		if (!empty) {
			hbox = new HBox(14, folderIcon);
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
			}
		}
	}
}
