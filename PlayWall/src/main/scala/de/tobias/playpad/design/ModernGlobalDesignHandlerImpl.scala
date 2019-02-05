package de.tobias.playpad.design

import java.nio.file.Files
import java.util.function.Consumer

import de.thecodelabs.utils.application.ApplicationUtils
import de.thecodelabs.utils.application.container.PathType
import de.tobias.playpad.design.modern.model.{ModernCartDesign, ModernGlobalDesign}
import de.tobias.playpad.design.modern.{ModernColor, ModernGlobalDesignHandler}
import de.tobias.playpad.pad.content.play.Durationable
import de.tobias.playpad.pad.viewcontroller.IPadViewController
import de.tobias.playpad.project.Project
import de.tobias.playpad.view.{ColorPickerView, PseudoClasses}
import de.tobias.playpad.viewcontroller.main.IMainViewController
import de.tobias.playpad.{DisplayableColor, PlayPadMain}
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.util.Duration
import org.springframework.expression.ExpressionParser
import org.springframework.expression.common.TemplateParserContext
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext

import scala.collection.JavaConverters._

class ModernGlobalDesignHandlerImpl extends ModernGlobalDesignHandler with ColorModeHandler {

	val styleSheets: Array[String] = Array(
		"style/components/button.css",
		"style/components/checkbox.css",
		"style/components/scrollbar.css",
		"style/components/textfield.css",
		"style/components/radiobutton.css",
		"style/components/list.css",
		"style/components/dialog.css"
	)

	override def applyStyleSheet(stage: Stage): Unit = {
		styleSheets.foreach(stage.getScene.getStylesheets.add)

		stage.getScene.getStylesheets.add("style/style.css")
		stage.getScene.getStylesheets.add("style/modern.css")

		// Custom style for playwall available
		val customCss = ApplicationUtils.getApplication.getPath(PathType.CONFIGURATION, "style.css")
		if (Files exists customCss) {
			stage.getScene.getStylesheets.add(customCss.toUri.toString)
		}
	}

	override def applyStyleSheetToMainViewController(design: ModernGlobalDesign, controller: IMainViewController, stage: Stage, project: Project): Unit = {
		applyStyleSheet(stage)

		controller.setGridColor(Color.TRANSPARENT)

		// generate dynamic style sheet
		val customCss = ApplicationUtils.getApplication.getPath(PathType.CONFIGURATION, "custom_style.css")
		val stringBuilder = new StringBuilder()
		stringBuilder.append(generateCss(design))

		val cartDesignHandler = PlayPadMain.getProgramInstance.getModernDesign.cart

		project.getPads.forEach(pad => {
			val padSettings = pad.getPadSettings

			if (padSettings.isCustomDesign) {
				val cartDesign = padSettings.getDesign
				stringBuilder.append(cartDesignHandler.generateCss(cartDesign, s"${pad.getPadIndex}", design.isFlatDesign))
			}
		})
		Files.write(customCss, stringBuilder.toString().getBytes())

		stage.getScene.getStylesheets.remove(customCss.toUri.toString)
		stage.getScene.getStylesheets.add(customCss.toUri.toString)
	}

	private def generateCss(design: ModernGlobalDesign): String = {
		generateCss(design, design.getBackgroundColor) +
			generateCss(design, design.getPlayColor, s":${PseudoClasses.PLAY_CLASS.getPseudoClassName}") +
			generateCss(design, design.getBackgroundColor, s":${PseudoClasses.WARN_CLASS.getPseudoClassName}")
	}

	private def generateCss(design: ModernGlobalDesign, color: ModernColor, styleState: String = ""): String = {
		val expressionParser: ExpressionParser = new SpelExpressionParser()
		val context = new StandardEvaluationContext()

		val resource = ApplicationUtils.getApplication.getClasspathResource("style/modern-global.css")
		val string = resource.getAsString

		val values = Map[String, AnyRef](
			"class" -> styleState,
			"buttonColor" -> color.getButtonColor,
			"playbarTrackColor" -> color.getPlaybarColor,
			"playbarBarColor" -> color.getPlaybarTrackColor,
			"padColor" -> (if (design.isFlatDesign) color.paint() else color.linearGradient()),
			"fontColor" -> color.getFontColor,
			"infoFontSize" -> s"${design.getInfoFontSize}",
			"titleFontSize" -> s"${design.getTitleFontSize}",
		)

		context.setVariables(values.asJava)
		expressionParser.parseExpression(string, new TemplateParserContext("${", "}")).getValue(context, classOf[String])
	}

	override def handleWarning(design: ModernGlobalDesign, controller: IPadViewController, warning: Duration): Unit = {
		if (design.isWarnAnimation) {
			warnAnimation(design, controller, warning)
		}
		else {
			ModernDesignAnimator.warnFlash(controller)
		}
	}

	private def warnAnimation(design: ModernGlobalDesign, controller: IPadViewController, warning: Duration): Unit = {
		val stopColor = if (design.isFlatDesign) design.getBackgroundColor.toFlatFadeableColor else design.getBackgroundColor.toFadeableColor
		val playColor = if (design.isFlatDesign) design.getPlayColor.toFlatFadeableColor else design.getPlayColor.toFadeableColor

		val pad = controller.getPad
		var duration = warning
		pad.getContent match {
			case durationable: Durationable =>
				if (warning.greaterThan(durationable.getDuration)) {
					duration = durationable.getDuration
				}
			case _ =>
		}
		ModernDesignAnimator.animateWarn(controller, playColor, stopColor, duration)
	}


	override def stopWarning(design: ModernGlobalDesign, controller: IPadViewController): Unit = ModernDesignAnimator.stopAnimation(controller)

	override def getColorInterface(onSelection: Consumer[DisplayableColor]) = new ColorPickerView(null, ModernColor.values.asInstanceOf[Array[DisplayableColor]], onSelection)

	override def setColor(design: ModernCartDesign, color: DisplayableColor): Unit = {
		color match {
			case c: ModernColor =>
				design.setBackgroundColor(c)
			case _ =>
		}
	}
}
