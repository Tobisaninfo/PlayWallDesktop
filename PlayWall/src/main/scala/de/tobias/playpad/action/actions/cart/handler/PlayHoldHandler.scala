package de.tobias.playpad.action.actions.cart.handler

import de.thecodelabs.midi.event.KeyEventType
import de.tobias.playpad.action.actions.CartAction
import de.tobias.playpad.pad.Pad

class PlayHoldHandler extends CartActionHandler {

	override def performAction(keyEventType: KeyEventType, cartAction: CartAction, pad: Pad): Unit = {
		if (keyEventType eq KeyEventType.DOWN) {
			if (pad.isReady) { // Allow the listener to send the feedback
				// cartAction.getPadPositionListener.setSend(false) TODO Fix
				pad.play()
			}
		} else if (keyEventType eq KeyEventType.UP) {
			if (pad.isPlay) {
				pad.stop()
			}
		}
	}
}
