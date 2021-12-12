package de.tobias.playpad.design

import de.thecodelabs.utils.application.ApplicationUtils
import de.thecodelabs.utils.application.container.PathType
import de.tobias.playpad.design.modern.model.{ModernCartDesign, ModernGlobalDesign}
import de.tobias.playpad.design.modern.{ModernColor, ModernGlobalDesignHandler}
import de.tobias.playpad.project.Project
import de.tobias.playpad.util.Minifier
import de.tobias.playpad.view.{ColorPickerView, PseudoClasses}
import de.tobias.playpad.viewcontroller.main.IMainViewController
import de.tobias.playpad.{DisplayableColor, PlayPadMain}
import javafx.scene.paint.Color
import javafx.stage.Stage
import org.springframework.expression.ExpressionParser
import org.springframework.expression.common.TemplateParserContext
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext

import java.nio.file.Files
import java.util.function.Consumer
import scala.jdk.CollectionConverters._

class ModernGlobalDesignHandlerImpl extends ModernGlobalDesignHandler with ColorModeHandler {

	override def applyStyleSheet(stage: Stage): Unit = {
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
		project.getPage(controller.getPage).getPads.forEach(pad => {
			val padSettings = pad.getPadSettings

			val cartDesign = padSettings.getDesign
			stringBuilder.append(cartDesignHandler.generateCss(cartDesign, s"${pad.getPadIndex}", design.isFlatDesign))
		})
		Files.write(customCss, stringBuilder.toString().getBytes())

		stage.getScene.getStylesheets.remove(customCss.toUri.toString)
		stage.getScene.getStylesheets.add(customCss.toUri.toString)
	}

	private def generateCss(design: ModernGlobalDesign): String = {
		String.join(
			generateCss(design, design.getBackgroundColor),
			generateCss(design, design.getPlayColor, s":${PseudoClasses.PLAY_CLASS.getPseudoClassName}"),
			generateCss(design, design.getBackgroundColor, s":${PseudoClasses.WARN_CLASS.getPseudoClassName}")
		)
	}

	private def generateCss(design: ModernGlobalDesign, color: ModernColor, styleState: String = ""): String = {
		val expressionParser: ExpressionParser = new SpelExpressionParser()
		val context = new StandardEvaluationContext()

		val resource = ApplicationUtils.getApplication.getClasspathResource("style/modern-global.css")
		val string = Minifier minify resource.getAsString

		val values: Map[String, AnyRef] = Map(
			"class" -> styleState,
			"buttonColor" -> color.getButtonColor,
			"playbarTrackColor" -> color.getPlaybarColor,
			"playbarBarColor" -> color.getPlaybarTrackColor,
			"padColor" -> (if (design.isFlatDesign) color.paint() else color.linearGradient()),
			"padCueInColor" -> (if (design.isFlatDesign) design.getCueInColor.paint() else design.getCueInColor.linearGradient()),
			"fontColor" -> color.getFontColor,
			"infoFontSize" -> s"${design.getInfoFontSize}",
			"titleFontSize" -> s"${design.getTitleFontSize}"
		)

		context.setVariables(values.asJava)
		expressionParser.parseExpression(string, new TemplateParserContext("${", "}")).getValue(context, classOf[String])
	}

	override def getColorInterface(onSelection: Consumer[DisplayableColor]) = new ColorPickerView(null, ModernColor.values.asInstanceOf[Array[DisplayableColor]], onSelection)

	override def setColor(design: ModernCartDesign, color: DisplayableColor): Unit = {
		color match {
			case c: ModernColor =>
				design.setBackgroundColor(c)
			case _ =>
		}
	}
}
