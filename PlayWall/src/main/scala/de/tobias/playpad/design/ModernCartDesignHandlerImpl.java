package de.tobias.playpad.design;

import de.thecodelabs.utils.application.ApplicationUtils;
import de.thecodelabs.utils.application.resources.classpath.ClasspathResource;
import de.tobias.playpad.design.modern.ModernCartDesignHandler;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.design.modern.model.ModernCartDesign;
import de.tobias.playpad.util.Minifier;
import de.tobias.playpad.view.PseudoClasses;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class ModernCartDesignHandlerImpl implements ModernCartDesignHandler
{
	@Override
	public String generateCss(ModernCartDesign design, String classSuffix, boolean flat)
	{
		String result = "";

		if(design.isEnableCustomBackgroundColor())
		{
			result += generatePadCss(design, flat, classSuffix, design.getBackgroundColor(), "");
			result += generatePadCss(design, flat, classSuffix, design.getBackgroundColor(), MessageFormat.format(":{0}", PseudoClasses.WARN_CLASS.getPseudoClassName()));
		}

		if(design.isEnableCustomPlayColor())
		{
			result += generatePadCss(design, flat, classSuffix, design.getPlayColor(), MessageFormat.format(":{0}", PseudoClasses.PLAY_CLASS.getPseudoClassName()));
			;
		}

		if(design.isEnableCustomCueInColor())
		{
			result += generateCueInCss(design, flat, classSuffix);
		}

		return result;
	}

	private String generatePadCss(ModernCartDesign design, Boolean flat, String padIdentifier, ModernColor color, String styleState)
	{
		final Map<String, Object> values = new HashMap<>();
		values.put("prefix", padIdentifier);
		values.put("class", styleState);
		values.put("buttonColor", color.getButtonColor());
		values.put("playbarTrackColor", color.getPlaybarColor());
		values.put("playbarBarColor", color.getPlaybarTrackColor());

		if(flat)
		{
			values.put("padColor", color.paint());
		}
		else
		{
			values.put("padColor", color.linearGradient());
		}

		if(flat)
		{
			values.put("padCueInColor", design.getCueInColor().paint());
		}
		else
		{
			values.put("padCueInColor", design.getCueInColor().linearGradient());
		}

		values.put("fontColor", color.getFontColor());

		return generateCss("style/modern-pad.css", values);
	}

	private String generateCueInCss(ModernCartDesign design, Boolean flat, String padIdentifier)
	{
		final Map<String, Object> values = new HashMap<>();
		values.put("prefix", padIdentifier);
		if(flat)
		{
			values.put("padCueInColor", design.getCueInColor().paint());
		}
		else
		{
			values.put("padCueInColor", design.getCueInColor().linearGradient());
		}

		return generateCss("style/modern-pad-cue-in.css", values);
	}

	private String generateCss(String templatePath, Map<String, Object> values)
	{
		final ExpressionParser expressionParser = new SpelExpressionParser();

		final StandardEvaluationContext context = new StandardEvaluationContext();

		final ClasspathResource resource = ApplicationUtils.getApplication().getClasspathResource(templatePath);
		final String content = Minifier.minify(resource.getAsString());

		context.setVariables(values);
		return expressionParser.parseExpression(content, new TemplateParserContext("${", "}")).getValue(context, String.class);
	}
}
