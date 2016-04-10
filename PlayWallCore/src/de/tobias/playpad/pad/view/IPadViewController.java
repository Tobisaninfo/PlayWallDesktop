package de.tobias.playpad.pad.view;

import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.viewcontroller.IPadView;

public interface IPadViewController {

	public IPadView getParent();

	public Pad getPad();

	public void setPad(Pad pad);

	public void connectPad();

	public void unconnectPad();

	public void showDnDLayout(boolean b);
}
