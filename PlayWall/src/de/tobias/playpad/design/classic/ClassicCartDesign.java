package de.tobias.playpad.design.classic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.GlobalDesign;
import de.tobias.playpad.design.Design;
import de.tobias.playpad.pad.view.IPadViewController;
import de.tobias.playpad.settings.Warning;
import de.tobias.playpad.viewcontroller.IPadView;
import de.tobias.utils.util.ColorXMLUtils;
import javafx.application.Platform;
import javafx.scene.paint.Color;

public class ClassicCartDesign extends Design implements CartDesign {

	public static final String TYPE = "classic";

	private Color backgroundColor = Color.TRANSPARENT;
	private Color playbackColor = Color.web("#ffb48bbb");
	private Color warnColor = Color.web("#ff8888bb");
	private Color fadeColor = Color.web("#ffea86bb");
	private Color accentColor = Color.BLACK;

	private Color infoLabelColor = Color.BLACK;
	private Color titleLabelColor = Color.BLACK;

	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public Color getPlaybackColor() {
		return playbackColor;
	}

	public void setPlaybackColor(Color playbackColor) {
		this.playbackColor = playbackColor;
	}

	public Color getWarnColor() {
		return warnColor;
	}

	public void setWarnColor(Color warnColor) {
		this.warnColor = warnColor;
	}

	public Color getFadeColor() {
		return fadeColor;
	}

	public void setFadeColor(Color fadeColor) {
		this.fadeColor = fadeColor;
	}

	public Color getInfoLabelColor() {
		return infoLabelColor;
	}

	public void setInfoLabelColor(Color idLabelColor) {
		this.infoLabelColor = idLabelColor;
	}

	public Color getTitleLabelColor() {
		return titleLabelColor;
	}

	public void setTitleLabelColor(Color titleLabelColor) {
		this.titleLabelColor = titleLabelColor;
	}

	public Color getAccentColor() {
		return accentColor;
	}

	public void setAccentColor(Color accentColor) {
		this.accentColor = accentColor;
	}

	public void reset() {
		backgroundColor = Color.TRANSPARENT;
		playbackColor = Color.web("#ffb48bbb");
		warnColor = Color.web("#ff8888bb");
		fadeColor = Color.web("#ffea86bb");

		accentColor = Color.BLACK;
		infoLabelColor = Color.BLACK;
		titleLabelColor = Color.BLACK;
	}

	public void load(Element rootElement) {
		setBackgroundColor(ColorXMLUtils.load(rootElement.element("BackgroundColor")));
		setPlaybackColor(ColorXMLUtils.load(rootElement.element("PlaybackColor")));
		setWarnColor(ColorXMLUtils.load(rootElement.element("WarnColor")));
		setFadeColor(ColorXMLUtils.load(rootElement.element("FadeColor")));
		setAccentColor(ColorXMLUtils.load(rootElement.element("AccentColor")));

		Element indexLabelEmement = rootElement.element("InfoLabel");
		if (indexLabelEmement != null) {
			setInfoLabelColor(ColorXMLUtils.load(indexLabelEmement.element("Color")));
		}

		Element titleLabelElement = rootElement.element("TitleLabel");
		if (titleLabelElement != null) {
			setTitleLabelColor(ColorXMLUtils.load(titleLabelElement.element("Color")));
		}
	}

	public void save(Element element) {
		ColorXMLUtils.save(element.addElement("BackgroundColor"), backgroundColor);
		ColorXMLUtils.save(element.addElement("PlaybackColor"), playbackColor);
		ColorXMLUtils.save(element.addElement("WarnColor"), warnColor);
		ColorXMLUtils.save(element.addElement("FadeColor"), fadeColor);
		ColorXMLUtils.save(element.addElement("AccentColor"), accentColor);

		Element indexLabelElement = element.addElement("InfoLabel");
		ColorXMLUtils.save(indexLabelElement.addElement("Color"), infoLabelColor);

		Element titleLabelElement = element.addElement("TitleLabel");
		ColorXMLUtils.save(titleLabelElement.addElement("Color"), titleLabelColor);
	}

	public void save(Path path) throws UnsupportedEncodingException, IOException {
		Document document = DocumentHelper.createDocument();
		Element rootElement = document.addElement("layout");
		save(rootElement);

		XMLWriter writer = new XMLWriter(Files.newOutputStream(path), OutputFormat.createPrettyPrint());
		writer.write(document);
		writer.close();
	}

	@Override
	public CartDesign clone() throws CloneNotSupportedException {
		ClassicCartDesign layout = (ClassicCartDesign) super.clone();

		layout.backgroundColor = Color.color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(),
				backgroundColor.getOpacity());
		layout.playbackColor = Color.color(playbackColor.getRed(), playbackColor.getGreen(), playbackColor.getBlue(),
				playbackColor.getOpacity());
		layout.warnColor = Color.color(warnColor.getRed(), warnColor.getGreen(), warnColor.getBlue(), warnColor.getOpacity());
		layout.fadeColor = Color.color(fadeColor.getRed(), fadeColor.getGreen(), fadeColor.getBlue(), fadeColor.getOpacity());
		layout.infoLabelColor = Color.color(infoLabelColor.getRed(), infoLabelColor.getGreen(), infoLabelColor.getBlue(),
				infoLabelColor.getOpacity());
		layout.titleLabelColor = Color.color(titleLabelColor.getRed(), titleLabelColor.getGreen(), titleLabelColor.getBlue(),
				titleLabelColor.getOpacity());

		return layout;
	}

	public String convertToCss(String classSufix, boolean fullCss) {
		StringBuilder builder = new StringBuilder();

		if (classSufix.isEmpty()) {
			builder.append(".fonticon {\n");
			builder.append("\t-fx-text-fill: " + accentColor + ";\n");
			builder.append("}\n");

			builder.append(".progress-bar {\n");
			builder.append("\t-fx-accent: " + accentColor + ";\n");
			builder.append("}\n");

			builder.append(".slider .thumb {\n");
			builder.append("-fx-base: " + accentColor + ";\n");
			builder.append("}\n");
		}

		if (fullCss) {
			builder.append(".pad" + classSufix + " {\n");
			builder.append("\t-fx-background-color: " + backgroundColor + ";\n");
			builder.append("}\n");

			builder.append(".pad" + classSufix + ":" + PseudoClasses.PLAY_CALSS.getPseudoClassName() + " {\n");
			builder.append("\t-fx-background-color: " + playbackColor + ";\n");
			builder.append("}\n");

			builder.append(".pad" + classSufix + ":" + PseudoClasses.WARN_CLASS.getPseudoClassName() + " {\n");
			builder.append("\t-fx-background-color: " + warnColor + ";\n");
			builder.append("}\n");

			builder.append(".pad" + classSufix + ":" + PseudoClasses.FADE_CLASS.getPseudoClassName() + " {\n");
			builder.append("\t-fx-background-color: " + fadeColor + ";\n");
			builder.append("}\n");

			builder.append(".pad" + classSufix + "-info {\n");
			builder.append("\t-fx-text-fill: " + infoLabelColor + ";\n");
			builder.append("}\n");

			builder.append(".pad" + classSufix + "-title {\n");
			builder.append("\t-fx-text-fill: " + titleLabelColor + ";\n");
			builder.append("}\n");

		}

		return builder.toString().replace("0x", "#");
	}

	@Override
	public void handleWarning(IPadViewController controller, Warning warning, GlobalDesign layout) {
		final IPadView view = controller.getParent();

		try {
			while (true) {
				Platform.runLater(() ->
				{
					view.pseudoClassState(PseudoClasses.WARN_CLASS, true);
					view.pseudoClassState(PseudoClasses.PLAY_CALSS, false);
				});
				Thread.sleep(500);
				Platform.runLater(() ->
				{
					view.pseudoClassState(PseudoClasses.PLAY_CALSS, true);
					view.pseudoClassState(PseudoClasses.WARN_CLASS, false);
				});
				Thread.sleep(500);
			}
		} catch (InterruptedException e) {
			Platform.runLater(() ->
			{
				view.pseudoClassState(PseudoClasses.WARN_CLASS, false);
				view.pseudoClassState(PseudoClasses.PLAY_CALSS, false);
			});
		}
	}

	@Override
	public void copyGlobalLayout(GlobalDesign globalLayout) {
		if (globalLayout instanceof ClassicGlobalDesign) {
			ClassicGlobalDesign classicGlobalLayout = (ClassicGlobalDesign) globalLayout;
			backgroundColor = classicGlobalLayout.getBackgroundColor();
			playbackColor = classicGlobalLayout.getPlaybackColor();
			warnColor = classicGlobalLayout.getWarnColor();
			fadeColor = classicGlobalLayout.getFadeColor();

			accentColor = classicGlobalLayout.getAccentColor();
			infoLabelColor = classicGlobalLayout.getInfoLabelColor();
			titleLabelColor = classicGlobalLayout.getAccentColor();
		}
	}
}
