package de.tobias.playpad.viewcontroller;

import java.util.ResourceBundle;

import de.tobias.playpad.layout.GlobalLayout;
import de.tobias.utils.ui.ContentViewController;

public abstract class GlobalLayoutViewController extends ContentViewController {

	public GlobalLayoutViewController(String name, String path, ResourceBundle localization, GlobalLayout layout) {
		super(name, path, localization);
	}
}
