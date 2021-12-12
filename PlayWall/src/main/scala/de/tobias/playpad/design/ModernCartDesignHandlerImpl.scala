package de.tobias.playpad.design

import de.thecodelabs.utils.application.ApplicationUtils
import de.tobias.playpad.design.modern.model.ModernCartDesign
import de.tobias.playpad.design.modern.{ModernCartDesignHandler, ModernColor}
import de.tobias.playpad.util.Minifier
import de.tobias.playpad.view.PseudoClasses
import org.springframework.expression.ExpressionParser
import org.springframework.expression.common.TemplateParserContext
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext

import scala.jdk.CollectionConverters._

class ModernCartDesignHandlerImpl extends ModernCartDesignHandler {

	override def generateCss(design: ModernCartDesign, classSuffix: String, flat: Boolean): String = {
		var result = ""

		if(design.isEnableCustomBackgroundColor)
		{
			result +=	generateCss(design, flat, classSuffix, design.getBackgroundColor)
			generateCss(design, flat, classSuffix, design.getBackgroundColor, s":${PseudoClasses.WARN_CLASS.getPseudoClassName}")
		}

		if(design.isEnableCustomPlayColor)
		{
			result += generateCss(design, flat, classSuffix, design.getPlayColor, s":${PseudoClasses.PLAY_CLASS.getPseudoClassName}")
		}

		result
	}

	private def generateCss(design: ModernCartDesign, flat: Boolean, padIdentifier: String, color: ModernColor, styleState: String = ""): String = {
		val expressionParser: ExpressionParser = new SpelExpressionParser()
		val context = new StandardEvaluationContext()

		val resource = ApplicationUtils.getApplication.getClasspathResource("style/modern-pad.css")
		val string = Minifier minify resource.getAsString

		val values: Map[String, AnyRef] = Map(
			"prefix" -> padIdentifier,
			"class" -> styleState,
			"buttonColor" -> color.getButtonColor,
			"playbarTrackColor" -> color.getPlaybarColor,
			"playbarBarColor" -> color.getPlaybarTrackColor,
			"padColor" -> (if (flat) color.paint() else color.linearGradient()),
			"padCueInColor" -> (if (flat) design.getCueInColor.paint() else design.getCueInColor.linearGradient()),
			"fontColor" -> color.getFontColor
		)

		context.setVariables(values.asJava)
		expressionParser.parseExpression(string, new TemplateParserContext("${", "}")).getValue(context, classOf[String])
	}
}
