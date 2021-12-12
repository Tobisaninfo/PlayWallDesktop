package de.tobias.playpad.design

import de.tobias.playpad.design.modern.ModernWarningDesignHandler
import de.tobias.playpad.design.modern.model.ModernGlobalDesign
import de.tobias.playpad.pad.viewcontroller.AbstractPadViewController
import de.tobias.playpad.util.FadeableColor
import javafx.util.Duration

class ModernWarningDesignHandlerImpl extends ModernWarningDesignHandler {

  override def performWarning(design: ModernGlobalDesign, fadeStartColor: FadeableColor, fadeStopColor: FadeableColor, controller: AbstractPadViewController, warningDuration: Duration): Unit = {
    if (design.isWarnAnimation) {
      ModernDesignAnimator.animateWarn(controller, fadeStartColor, fadeStopColor, warningDuration)
    } else {
      ModernDesignAnimator.warnFlash(controller)
    }
  }

  override def stopWarning(controller: AbstractPadViewController): Unit = ModernDesignAnimator.stopAnimation(controller)
}
