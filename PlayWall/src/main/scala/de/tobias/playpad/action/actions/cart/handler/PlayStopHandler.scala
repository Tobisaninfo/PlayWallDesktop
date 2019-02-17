package de.tobias.playpad.action.actions.cart.handler

import de.tobias.playpad.action.InputType
import de.tobias.playpad.action.actions.cart.CartAction
import de.tobias.playpad.pad.Pad

class PlayStopHandler extends CartActionHandler {

	override def performAction(inputType: InputType, cartAction: CartAction, pad: Pad): Unit = {
		if (inputType eq InputType.PRESSED) {
			if (pad.isPlay) {
				pad.stop()
			} else { // Allow the listener to send the feedback
				cartAction.getPadPositionListener.setSend(false)
				pad.play()
			}
		}
	}
}
