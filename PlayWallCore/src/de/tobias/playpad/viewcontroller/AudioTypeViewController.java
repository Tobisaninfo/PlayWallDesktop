package de.tobias.playpad.viewcontroller;

import java.util.ResourceBundle;

import de.tobias.utils.ui.ContentViewController;


public abstract class AudioTypeViewController extends ContentViewController {

	public AudioTypeViewController(String name, String path, ResourceBundle localization) {
		super(name, path, localization);
	}
	
	public abstract boolean isChanged();
}
