package de.tobias.playpad.server.sync.listener.upstream;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.project.page.Page;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.command.Change;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.ServerUtils;
import javafx.beans.value.ChangeListener;

import java.util.function.Consumer;

/**
 * Listen to the properties of Project to send changes to the server.
 */
public class PageUpdateListener {

	private Page page;

	private ChangeListener<String> nameListener;
	private ChangeListener<Number> positionListener;

	public PageUpdateListener(Page page) {
		this.page = page;

		nameListener = (observable, oldValue, newValue) -> {
			Change change = new Change(PropertyDef.PAGE_NAME, newValue, page);
			CommandManager.execute(Commands.PAGE_UPDATE, page.getProject().getProjectReference(), change);
		};

		positionListener = (observable, oldValue, newValue) -> {
			Change change = new Change(PropertyDef.PAGE_POSITION, newValue, page);
			CommandManager.execute(Commands.PAGE_UPDATE, page.getProject().getProjectReference(), change);
		};
	}

	private boolean added;

	public void addListener() {
		if (!added) {
			added = true;
			page.nameProperty().addListener(nameListener);
			page.positionProperty().addListener(positionListener);
		}
	}

	public void removeListener() {
		added = false;
		page.nameProperty().addListener(nameListener);
		page.positionProperty().removeListener(positionListener);
	}
}
