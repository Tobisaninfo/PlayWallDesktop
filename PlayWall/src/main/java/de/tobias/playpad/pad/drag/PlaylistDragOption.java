package de.tobias.playpad.pad.drag;

import de.thecodelabs.utils.ui.icon.FontAwesomeType;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.pad.Pad;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

import java.io.File;
import java.util.List;

public class PlaylistDragOption implements ContentDragOption {

	private final StringProperty displayProperty = new SimpleStringProperty(Localization.getString("DndMode.Playlist"));

	@Override
	public void handleDrop(Pad currentPad, List<File> files) {
		if (currentPad.isPadVisible()) {
			currentPad.getController().getView().showBusyView(true);
		}

		for (File file : files) {
			currentPad.addPath(file.toPath());
		}
	}

	@Override
	public StringProperty displayProperty() {
		return displayProperty;
	}

	@Override
	public Node getGraphics() {
		return new FontIcon(FontAwesomeType.PLUS_CIRCLE);
	}
}
