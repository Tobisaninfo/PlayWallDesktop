package de.tobias.playpad.view.main;

import de.tobias.playpad.viewcontroller.IPadView;
import de.tobias.playpad.viewcontroller.main.IMenuToolbarViewController;

public interface MainLayoutConnect {

	public String getType();
	
	public String name();
	
	public IMenuToolbarViewController createMenuToolbar();
	
	public IPadView createPadView();
}
