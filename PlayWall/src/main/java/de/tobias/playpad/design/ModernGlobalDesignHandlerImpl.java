package de.tobias.playpad.design;

import de.thecodelabs.logger.Logger;
import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.container.PathType;
import de.thecodelabs.utils.application.resources.classpath.ClasspathResource;
import de.tobias.playpad.DisplayableColor;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.design.modern.ModernCartDesignHandler;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.design.modern.ModernGlobalDesignHandler;
import de.tobias.playpad.design.modern.model.ModernCartDesign;
import de.tobias.playpad.design.modern.model.ModernGlobalDesign;
import de.tobias.playpad.pad.PadSettings;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.util.Minifier;
import de.tobias.playpad.view.ColorPickerView;
import de.tobias.playpad.view.PseudoClasses;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ModernGlobalDesignHandlerImpl implements ModernGlobalDesignHandler, ColorModeHandler
{
	@Override
	public void applyStyleSheet(Stage stage)
	{
		stage.getScene().getStylesheets().add("style/style.css");
		stage.getScene().getStylesheets().add("style/modern.css");

		// Custom style for playwall available
		final Path customCss = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "style.css");
		if(Files.exists(customCss))
		{
			stage.getScene().getStylesheets().add(customCss.toUri().toString());
		}
	}

	@Override
	public void applyStyleSheetToMainViewController(ModernGlobalDesign design, IMainViewController controller, Stage stage, Project project)
	{
		applyStyleSheet(stage);

		controller.setGridColor(Color.TRANSPARENT);

		// generate dynamic style sheet
		final Path customCss = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "custom_style.css");
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(generateCss(design));

		final ModernCartDesignHandler cartDesignHandler = PlayPadMain.getProgramInstance().getModernDesign().cart();
		project.getPage(controller.getPage()).getPads().forEach(pad -> {
			final PadSettings padSettings = pad.getPadSettings();

			final ModernCartDesign cartDesign = padSettings.getDesign();
			stringBuilder.append(cartDesignHandler.generateCss(cartDesign, String.valueOf(pad.getPadIndex()), design.isFlatDesign()));
		});

		try
		{
			Files.write(customCss, stringBuilder.toString().getBytes());
		}
		catch(IOException e)
		{
			Logger.error(e);
		}

		stage.getScene().getStylesheets().remove(customCss.toUri().toString());
		stage.getScene().getStylesheets().add(customCss.toUri().toString());
	}

	private String generateCss(ModernGlobalDesign design)
	{
		return String.join(
				generateCss(design, design.getBackgroundColor(), ""),
				generateCss(design, design.getPlayColor(), MessageFormat.format(":{0}", PseudoClasses.PLAY_CLASS.getPseudoClassName())),
				generateCss(design, design.getBackgroundColor(), MessageFormat.format(":{0}", PseudoClasses.WARN_CLASS.getPseudoClassName()))
		);
	}

	private String generateCss(ModernGlobalDesign design, ModernColor color, String styleState)
	{
		final ExpressionParser expressionParser = new SpelExpressionParser();
		final StandardEvaluationContext context = new StandardEvaluationContext();

		final ClasspathResource resource = ApplicationUtils.getApplication().getClasspathResource("style/modern-global.css");
		final String content = Minifier.minify(resource.getAsString());

		final Map<String, Object> values = new HashMap<>();
		values.put("class", styleState);
		values.put("buttonColor", color.getButtonColor());
		values.put("playbarTrackColor", color.getPlaybarColor());
		values.put("playbarBarColor", color.getPlaybarTrackColor());

		if(design.isFlatDesign())
		{
			values.put("padColor", color.paint());
		}
		else
		{
			values.put("padColor", color.linearGradient());
		}

		if(design.isFlatDesign())
		{
			values.put("padCueInColor", design.getCueInColor().paint());
		}
		else
		{
			values.put("padCueInColor", design.getCueInColor().linearGradient());
		}

		values.put("fontColor", color.getFontColor());
		values.put("infoFontSize", String.valueOf(design.getInfoFontSize()));
		values.put("titleFontSize", String.valueOf(design.getTitleFontSize()));

		context.setVariables(values);
		return expressionParser.parseExpression(content, new TemplateParserContext("${", "}")).getValue(context, String.class);
	}

	@Override
	public Node getColorInterface(Consumer<DisplayableColor> onSelection)
	{
		return new ColorPickerView(null, ModernColor.values(), onSelection);
	}

	@Override
	public void setColor(ModernCartDesign design, DisplayableColor color)
	{
		if(color instanceof ModernColor)
		{
			design.setBackgroundColor((ModernColor) color);
		}
	}
}
