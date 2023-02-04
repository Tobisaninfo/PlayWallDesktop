package de.tobias.playpad.action.actions.cart.handler;

import de.tobias.playpad.action.actions.CartAction;

public class CartActionHandlerFactory
{
	private CartActionHandlerFactory()
	{
	}

	public static CartActionHandler getInstance(CartAction.CartActionMode cartActionMode)
	{
		switch(cartActionMode)
		{
			case PLAY_PAUSE:
				return new PlayPauseHandler();
			case PLAY_STOP:
				return new PlayStopHandler();
			case PLAY_HOLD:
				return new PlayHoldHandler();
			case PLAY_PLAY:
				return new PlayPlayHandler();
			default:
				return null;
		}
	}
}
