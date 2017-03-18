package de.tobias.playpad.server.sync.listener.upstream;

import com.google.gson.JsonObject;
import de.tobias.playpad.PlayPadPlugin;
import de.tobias.playpad.design.modern.ModernCartDesign;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.server.Server;
import de.tobias.playpad.server.sync.PropertyDef;
import de.tobias.playpad.server.sync.ServerUtils;
import de.tobias.playpad.server.sync.command.Change;
import de.tobias.playpad.server.sync.command.CommandManager;
import de.tobias.playpad.server.sync.command.Commands;
import javafx.beans.value.ChangeListener;

import java.util.function.Consumer;

/**
 * Created by tobias on 24.02.17.
 */
public class DesignUpdateListener {

	private ModernCartDesign design;

	private ChangeListener<ModernColor> backgroundColorListener;
	private ChangeListener<ModernColor> playColorListener;

	public DesignUpdateListener(ModernCartDesign design) {
		this.design = design;

		backgroundColorListener = (observable, oldValue, newValue) -> {
			Change change = new Change(PropertyDef.DESIGN_BACKGROUND_COLOR, newValue, design);
			CommandManager.execute(Commands.DESIGN_UPDATE, design.getPad().getProject().getProjectReference(), change);
		};

		playColorListener = (observable, oldValue, newValue) -> {
			Change change = new Change(PropertyDef.DESIGN_PLAY_COLOR, newValue, design);
			CommandManager.execute(Commands.DESIGN_UPDATE, design.getPad().getProject().getProjectReference(), change);
		};
	}

	private boolean added;

	public void addListener() {
		if (!added) {
			added = true;
			design.backgroundColorProperty().addListener(backgroundColorListener);
			design.playColorProperty().addListener(playColorListener);
		}
	}

	public void removeListener() {
		added = false;
		design.backgroundColorProperty().removeListener(backgroundColorListener);
		design.playColorProperty().removeListener(playColorListener);
	}
}
