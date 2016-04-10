package de.tobias.playpad.action.mapper;

import java.util.ResourceBundle;

import de.tobias.utils.ui.ContentViewController;

public abstract class MapperViewController extends ContentViewController {

	public MapperViewController(String name, String path, ResourceBundle localization) {
		super(name, path, localization);
	}

	public abstract void showFeedback();

	public abstract void hideFeedback();

	public abstract Mapper getMapper();

	public abstract void showInputMapperUI();
}
