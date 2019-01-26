package de.tobias.playpad.action.actions.cart.handler

import de.tobias.playpad.action.actions.cart.CartAction.CartActionMode

object CartActionHandlerFactory {

	def getInstance(cartActionMode: CartActionMode): CartActionHandler = {
		cartActionMode match {
			case CartActionMode.PLAY_STOP => new PlayStopHandler()
			case CartActionMode.PLAY_PAUSE => new PlayPauseHandler()
			case CartActionMode.PLAY_HOLD => new PlayHoldHandler()
			case CartActionMode.PLAY_PLAY => new PlayPlayHandler()
		}
	}
}
