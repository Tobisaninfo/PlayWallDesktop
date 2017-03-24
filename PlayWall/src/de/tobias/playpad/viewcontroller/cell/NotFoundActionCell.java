package de.tobias.playpad.viewcontroller.cell;

import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.Strings;
import de.tobias.playpad.layout.desktop.pad.DesktopPadViewController;
import de.tobias.playpad.pad.content.PadContentRegistry;
import de.tobias.playpad.viewcontroller.dialog.NotFoundDialog;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import de.tobias.utils.util.Localization;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;

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
				FileChooser chooser = new FileChooser();
				PadContentRegistry registry = PlayPadPlugin.getRegistryCollection().getPadContents();

				// File Extension
				FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(Localization.getString(Strings.File_Filter_Media),
						registry.getSupportedFileTypes());
				chooser.getExtensionFilters().add(extensionFilter);

				// Last Folder
				Object openFolder = ApplicationUtils.getApplication().getUserDefaults().getData(DesktopPadViewController.OPEN_FOLDER);
				if (openFolder != null) {
					File folder = new File(openFolder.toString());
					chooser.setInitialDirectory(folder);
				}

				File file = chooser.showOpenDialog(((Node) event.getTarget()).getScene().getWindow());
				if (file != null) {
					Path path = file.toPath();
					currentItem.setLocalPath(path);
					currentItem.setSelected(true);

					// Search for new local paths
					parentDialog.find(true);
				}
			}
		}
	}
}
