package de.tobias.playpad.viewcontroller;

import java.util.ResourceBundle;

import de.tobias.playpad.layout.CartLayout;
import de.tobias.utils.ui.ContentViewController;

public abstract class CartLayoutViewController extends ContentViewController {

	public CartLayoutViewController(String name, String path, ResourceBundle localization, CartLayout layout) {
		super(name, path, localization);
	}
}
