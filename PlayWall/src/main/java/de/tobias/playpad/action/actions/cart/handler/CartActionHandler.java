package de.tobias.playpad.action.actions.cart.handler;

import de.thecodelabs.midi.event.KeyEventType;
import de.tobias.playpad.action.actions.CartAction;
import de.tobias.playpad.pad.Pad;

public interface CartActionHandler
{
	void performAction(KeyEventType keyEventType, CartAction cartAction, Pad pad);
}
