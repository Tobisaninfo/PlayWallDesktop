package de.tobias.playpad.action.actions.cart.handler;

import de.thecodelabs.midi.event.KeyEventType;
import de.tobias.playpad.action.actions.CartAction;
import de.tobias.playpad.pad.Pad;

public class PlayHoldHandler implements CartActionHandler
{
	@Override
	public void performAction(KeyEventType keyEventType, CartAction cartAction, Pad pad)
	{
		if(keyEventType.equals(KeyEventType.DOWN))
		{
			if(pad.isReady())
			{
				pad.play();
			}
		}
		else if(keyEventType.equals(KeyEventType.UP))
		{
			if(pad.isPlay())
			{
				pad.stop();
			}
		}
	}
}