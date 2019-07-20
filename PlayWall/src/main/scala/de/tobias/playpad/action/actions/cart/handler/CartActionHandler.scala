package de.tobias.playpad.action.actions.cart.handler

import de.thecodelabs.midi.event.KeyEventType
import de.tobias.playpad.action.actions.CartAction
import de.tobias.playpad.pad.Pad

trait CartActionHandler {

	def performAction(keyEventType: KeyEventType, cartAction: CartAction, pad: Pad): Unit
}
