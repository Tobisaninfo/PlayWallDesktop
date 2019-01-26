package de.tobias.playpad.action.actions.cart.handler

import de.tobias.playpad.action.InputType
import de.tobias.playpad.action.actions.cart.CartAction
import de.tobias.playpad.pad.Pad
import de.tobias.playpad.project.Project

trait CartActionHandler {

	def performAction(inputType: InputType, cartAction: CartAction, pad: Pad, project: Project): Unit
}
