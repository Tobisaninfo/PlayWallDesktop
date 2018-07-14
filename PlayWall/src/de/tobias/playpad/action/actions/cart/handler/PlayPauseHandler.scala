package de.tobias.playpad.action.actions.cart.handler

import de.tobias.playpad.action.InputType
import de.tobias.playpad.action.actions.cart.CartAction
import de.tobias.playpad.pad.Pad
import de.tobias.playpad.project.Project
import de.tobias.playpad.viewcontroller.main.IMainViewController

class PlayPauseHandler extends CartActionHandler {
    override def performAction(`type`: InputType,
                               cartAction: CartAction,
                               pad: Pad,
                               project: Project,
                               mainViewController: IMainViewController) = {
        if (`type` eq InputType.PRESSED) {
            if (pad.isPlay) {
                pad.pause()
            }
            else { // Allow the listener to send the feedback
                cartAction.getPadPositionListener.setSend(false)
                pad.play()
            }
        }
    }
}
