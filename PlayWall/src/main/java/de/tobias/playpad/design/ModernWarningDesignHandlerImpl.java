package de.tobias.playpad.design;

import de.tobias.playpad.design.modern.ModernWarningDesignHandler;
import de.tobias.playpad.design.modern.model.ModernGlobalDesign;
import de.tobias.playpad.pad.viewcontroller.AbstractPadViewController;
import de.tobias.playpad.project.api.IPad;
import de.tobias.playpad.util.FadeableColor;
import javafx.util.Duration;

public class ModernWarningDesignHandlerImpl implements ModernWarningDesignHandler
{
	@Override
	public void performWarning(ModernGlobalDesign design, FadeableColor fadeStartColor, FadeableColor fadeStopColor, AbstractPadViewController controller, Duration warningDuration)
	{
		if(design.isWarnAnimation())
		{
			ModernDesignAnimator.animateWarn(controller.getPad(), controller.getView(), fadeStartColor, fadeStopColor, warningDuration);
		}
		else
		{
			ModernDesignAnimator.warnFlash(controller.getView());
		}
	}

	@Override
	public void stopWarning(IPad pad)
	{
		ModernDesignAnimator.stopAnimation(pad);
	}
}
