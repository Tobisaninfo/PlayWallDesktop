package de.tobias.playpad.action.actions.cart.handler

import de.tobias.playpad.action.InputType
import de.tobias.playpad.action.actions.cart.CartAction
import de.tobias.playpad.pad.{Pad, PadStatus}
import de.tobias.playpad.project.Project
import de.tobias.playpad.viewcontroller.main.IMainViewController

class PlayPlayHandler extends CartActionHandler {
	override def performAction(`type`: InputType,
							   cartAction: CartAction,
							   pad: Pad,
							   project: Project,
							   mainViewController: IMainViewController) = {
		if (`type` eq InputType.PRESSED) {
			pad.setStatus(PadStatus.RESTART);
		}
	}
}
