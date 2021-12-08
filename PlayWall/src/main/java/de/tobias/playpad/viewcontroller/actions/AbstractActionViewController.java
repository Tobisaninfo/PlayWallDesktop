package de.tobias.playpad.viewcontroller.actions;

import de.thecodelabs.midi.action.Action;
import de.thecodelabs.utils.ui.NVC;

public abstract class AbstractActionViewController extends NVC {
	public abstract void setCartAction(Action action);
}
