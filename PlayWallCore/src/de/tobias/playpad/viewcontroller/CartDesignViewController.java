package de.tobias.playpad.viewcontroller;

import java.util.ResourceBundle;

import de.tobias.playpad.design.CartDesign;
import de.tobias.utils.ui.ContentViewController;

public abstract class CartDesignViewController extends ContentViewController {

	public CartDesignViewController(String name, String path, ResourceBundle localization, CartDesign layout) {
		super(name, path, localization);
	}
}
