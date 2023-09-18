package de.tobias.playpad.action.actions.cart.handler;

import de.thecodelabs.midi.event.KeyEventType;
import de.tobias.playpad.action.actions.CartAction;
import de.tobias.playpad.pad.Pad;

public class PlayPauseHandler implements CartActionHandler
{
	@Override
	public void performAction(KeyEventType keyEventType, CartAction cartAction, Pad pad)
	{
		if(keyEventType.equals(KeyEventType.DOWN))
		{
			if(pad.isPlay())
			{
				pad.pause();
			}
			else
			{ // Allow the listener to send the feedback
				// cartAction.getPadPositionListener.setSend(false) TODO Fix
				pad.play();
			}
		}
	}
}
