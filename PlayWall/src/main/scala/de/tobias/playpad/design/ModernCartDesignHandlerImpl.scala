package de.tobias.playpad.design

import de.thecodelabs.utils.application.ApplicationUtils
import de.tobias.playpad.design.modern.{ModernCartDesign, ModernCartDesignHandler, ModernColor, ModernGlobalDesign}
import de.tobias.playpad.pad.content.play.Durationable
import de.tobias.playpad.pad.viewcontroller.IPadViewController
import de.tobias.playpad.view.PseudoClasses
import javafx.util.Duration
import org.springframework.expression.ExpressionParser
import org.springframework.expression.common.TemplateParserContext
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext

import scala.collection.JavaConverters._

class ModernCartDesignHandlerImpl extends ModernCartDesignHandler {

	override def convertToCss(design: ModernCartDesign, classSuffix: String, flat: Boolean): String = {
		generateCss(design, flat, classSuffix, design.getBackgroundColor) +
			generateCss(design, flat, classSuffix, design.getPlayColor, s":${PseudoClasses.PLAY_CLASS.getPseudoClassName}") +
			generateCss(design, flat, classSuffix, design.getBackgroundColor, s":${PseudoClasses.WARN_CLASS.getPseudoClassName}")
	}

	private def generateCss(design: ModernCartDesign, flat: Boolean, padIdentifier: String, color: ModernColor, styleState: String = ""): String = {
		val expressionParser: ExpressionParser = new SpelExpressionParser()
		val context = new StandardEvaluationContext()

		val resource = ApplicationUtils.getApplication.getClasspathResource("style/modern-pad.css")
		val string = resource.getAsString

		val values = Map[String, AnyRef](
			"prefix" -> padIdentifier,
			"class" -> styleState,
			"buttonColor" -> color.getButtonColor,
			"playbarTrackColor" -> color.getPlaybarColor,
			"playbarBarColor" -> color.getPlaybarTrackColor,
			"padColor" -> (if (flat) color.paint() else color.linearGradient()),
			"fontColor" -> color.getFontColor
		)

		context.setVariables(values.asJava)
		expressionParser.parseExpression(string, new TemplateParserContext("${", "}")).getValue(context, classOf[String])
	}

	override def handleWarning(design: ModernCartDesign, controller: IPadViewController, warning: Duration, globalDesign: ModernGlobalDesign): Unit = {
		if (globalDesign.isWarnAnimation) {
			val playColor = design.getPlayColor
			val backgroundColor = design.getBackgroundColor

			val fadeStopColor = if (globalDesign.isFlatDesign) backgroundColor.toFlatFadeableColor else backgroundColor.toFadeableColor
			val fadePlayColor = if (globalDesign.isFlatDesign) playColor.toFlatFadeableColor else playColor.toFadeableColor

			var animationDuration = warning
			val pad = controller.getPad
			pad.getContent match {
				case durationable: Durationable =>
					if (warning greaterThan durationable.getDuration) {
						animationDuration = durationable.getDuration
					}
				case _ =>
			}
			ModernDesignAnimator.animateWarn(controller, fadePlayColor, fadeStopColor, animationDuration)
		} else {
			ModernDesignAnimator.warnFlash(controller)
		}
	}

	override def stopWarning(design: ModernCartDesign, controller: IPadViewController): Unit = ModernDesignAnimator.stopAnimation(controller)
}
