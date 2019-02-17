package de.tobias.playpad.action.actions.cart.handler

import de.tobias.playpad.action.InputType
import de.tobias.playpad.action.actions.cart.CartAction
import de.tobias.playpad.pad.{Pad, PadStatus}

class PlayPlayHandler extends CartActionHandler {

	override def performAction(inputType: InputType, cartAction: CartAction, pad: Pad): Unit = {
		if (inputType eq InputType.PRESSED) {
			pad.setStatus(PadStatus.RESTART)
		}
	}
}
