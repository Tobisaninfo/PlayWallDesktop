package de.tobias.playpad.viewcontroller;

import java.util.ResourceBundle;

import de.tobias.playpad.design.GlobalDesign;
import de.tobias.utils.ui.ContentViewController;

public abstract class GlobalDesignViewController extends ContentViewController {

	public GlobalDesignViewController(String name, String path, ResourceBundle localization, GlobalDesign layout) {
		super(name, path, localization);
	}
}
