package de.tobias.playpad.action.actions.cart.handler;

import de.thecodelabs.midi.event.KeyEventType;
import de.tobias.playpad.action.actions.CartAction;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.PadStatus;

public class PlayPlayHandler implements CartActionHandler
{

	@Override
	public void performAction(KeyEventType keyEventType, CartAction cartAction, Pad pad)
	{
		if(keyEventType.equals(KeyEventType.DOWN))
		{
			pad.setStatus(PadStatus.RESTART);
		}
	}
}
