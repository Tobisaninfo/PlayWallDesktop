package de.tobias.playpad.server.sync.listener.upstream;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.command.Change;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import javafx.beans.value.ChangeListener;

/**
 * Listen to the properties of Project to send changes to the server.
 */
public class PadUpdateListener {

	private Pad pad;

	private ChangeListener<String> nameListener;
	private ChangeListener<Number> positionListener;
	private ChangeListener<String> contentTypeListener;

	public PadUpdateListener(Pad pad) {
		this.pad = pad;

		nameListener = (observable, oldValue, newValue) -> {
			Change change = new Change(PropertyDef.PAD_NAME, newValue, pad);
			CommandManager.execute(Commands.PAD_UPDATE, pad.getProject().getProjectReference(), change);
		};

		positionListener = (observable, oldValue, newValue) -> {
			Change change = new Change(PropertyDef.PAD_POSITION, newValue, pad);
			CommandManager.execute(Commands.PAD_UPDATE, pad.getProject().getProjectReference(), change);
		};

		contentTypeListener = (observable, oldValue, newValue) -> {
			Change change = new Change(PropertyDef.PAD_CONTENT_TYPE, newValue, pad);
			CommandManager.execute(Commands.PAD_UPDATE, pad.getProject().getProjectReference(), change);
		};
	}

	private boolean added;

	public void addListener() {
		if (!added) {
			added = true;
			pad.nameProperty().addListener(nameListener);
			pad.positionProperty().addListener(positionListener);
			pad.contentTypeProperty().addListener(contentTypeListener);
		}
	}

	public void removeListener() {
		added = false;
		pad.nameProperty().addListener(nameListener);
		pad.positionProperty().removeListener(positionListener);
		pad.contentTypeProperty().removeListener(contentTypeListener);
	}
}
