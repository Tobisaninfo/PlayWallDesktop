package de.tobias.playpad.design.modern;

import de.tobias.playpad.design.modern.model.ModernCartDesign;
import de.tobias.playpad.design.modern.model.ModernGlobalDesign;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.content.play.Durationable;
import de.tobias.playpad.pad.viewcontroller.AbstractPadViewController;
import de.tobias.playpad.util.FadeableColor;
import javafx.util.Duration;

public interface ModernDesignHandlerParent
{
	default void handleWarning(ModernGlobalDesign globalDesign, ModernCartDesign cartDesign, AbstractPadViewController controller, Duration warningDuration)
	{
		ModernColor backgroundColor = globalDesign.getBackgroundColor();
		ModernColor playColor = globalDesign.getPlayColor();

		if(cartDesign.isEnableCustomBackgroundColor()) {
			backgroundColor = cartDesign.getBackgroundColor();
		}

		if(cartDesign.isEnableCustomPlayColor()) {
			playColor = cartDesign.getPlayColor();
		}

		final FadeableColor fadeStartColor = globalDesign.isFlatDesign() ? playColor.toFlatFadeableColor() : playColor.toFadeableColor();
		final FadeableColor fadeStopColor = globalDesign.isFlatDesign() ? backgroundColor.toFlatFadeableColor() : backgroundColor.toFadeableColor();
		final Duration duration = determineDuration(controller.getPad(), warningDuration);

		performWarning(globalDesign, fadeStartColor, fadeStopColor, controller, duration);
	}

	default Duration determineDuration(Pad pad, Duration warningDuration)
	{
		if(pad.getContent() instanceof Durationable)
		{
			final Durationable durationable = (Durationable) pad;
			if(warningDuration.greaterThan(durationable.getDuration()))
			{
				return durationable.getDuration();
			}
		}

		return warningDuration;
	}

	/*
	 * Wird in einem neuen Thread aufgerufen
	 */
	void performWarning(ModernGlobalDesign design, FadeableColor fadeStartColor, FadeableColor fadeStopColor, AbstractPadViewController controller, Duration duration);

	default void stopWarning(AbstractPadViewController controller) {
	}
}
