package de.tobias.playpad.action.actions.cart.handler

import de.thecodelabs.midi.event.KeyEventType
import de.tobias.playpad.action.actions.CartAction
import de.tobias.playpad.pad.{Pad, PadStatus}

class PlayPlayHandler extends CartActionHandler {

	override def performAction(keyEventType: KeyEventType, cartAction: CartAction, pad: Pad): Unit = {
		if (keyEventType eq KeyEventType.DOWN) {
			pad.setStatus(PadStatus.RESTART)
		}
	}
}
