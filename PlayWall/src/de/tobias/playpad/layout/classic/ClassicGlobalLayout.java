package de.tobias.playpad.layout.classic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.dom4j.Element;

import de.tobias.playpad.PseudoClasses;
import de.tobias.playpad.layout.CartLayout;
import de.tobias.playpad.layout.GlobalLayout;
import de.tobias.playpad.layout.Layout;
import de.tobias.playpad.pad.Pad;
import de.tobias.playpad.pad.view.IPadViewController;
import de.tobias.playpad.project.Project;
import de.tobias.playpad.settings.Warning;
import de.tobias.playpad.viewcontroller.IPadView;
import de.tobias.playpad.viewcontroller.main.IMainViewController;
import de.tobias.utils.application.ApplicationUtils;
import de.tobias.utils.application.container.PathType;
import de.tobias.utils.util.ColorXMLUtils;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ClassicGlobalLayout extends Layout implements GlobalLayout {

	public static final double minWidth = 180;
	public static final double minHeight = 100;

	public static final String TYPE = "classic";

	private ObjectProperty<Theme> themeProperty = new SimpleObjectProperty<>(Theme.LIGHT);
	private boolean customLayout = false;

	private Color backgroundColor = Color.TRANSPARENT;
	private Color playbackColor = Color.web("#ffb48bbb");
	private Color warnColor = Color.web("#ff8888bb");
	private Color fadeColor = Color.web("#ffea86bb");

	private Color accentColor = Color.BLACK;

	private int infoLabelFontSize = 13;
	private Color infoLabelColor = Color.BLACK;

	private int titleLabelFontSize = 13;
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

	public int getInfoLabelFontSize() {
		return infoLabelFontSize;
	}

	public void setInfoLabelFontSize(int idLabelFontSize) {
		this.infoLabelFontSize = idLabelFontSize;
	}

	public int getTitleLabelFontSize() {
		return titleLabelFontSize;
	}

	public void setTitleLabelFontSize(int titleLabelFontSize) {
		this.titleLabelFontSize = titleLabelFontSize;
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

	public Theme getTheme() {
		return themeProperty.get();
	}

	public void setTheme(Theme theme) {
		themeProperty.set(theme);
	}

	public ObjectProperty<Theme> themeProperty() {
		return themeProperty;
	}

	public boolean isCustomLayout() {
		return customLayout;
	}

	public void setCustomLayout(boolean customLayout) {
		this.customLayout = customLayout;
	}

	@Override
	public double getMinHeight(int rows) {
		return rows * minHeight;
	}

	@Override
	public double getMinWidth(int columns) {
		return columns * minWidth;
	}

	@Override
	public double getPadHeight() {
		return minHeight;
	}

	@Override
	public double getPadWidth() {
		return minWidth;
	}

	public void reset() {
		themeProperty = new SimpleObjectProperty<>(Theme.LIGHT);
		customLayout = false;

		backgroundColor = Color.TRANSPARENT;
		playbackColor = Color.web("#ffb48bbb");
		warnColor = Color.web("#ff8888bb");
		fadeColor = Color.web("#ffea86bb");

		accentColor = Color.BLACK;

		infoLabelFontSize = 13;
		infoLabelColor = Color.BLACK;

		titleLabelFontSize = 13;
		titleLabelColor = Color.BLACK;
	}

	@Override
	public void load(Element rootElement) {
		if (rootElement.element("Theme") != null)
			try {
				setTheme(Theme.valueOf(rootElement.element("Theme").getStringValue()));
			} catch (IllegalArgumentException e) {
				setTheme(Theme.LIGHT);
			}

		if (rootElement.element("CustomLayout") != null)
			setCustomLayout(Boolean.valueOf(rootElement.element("CustomLayout").getStringValue()));

		setBackgroundColor(ColorXMLUtils.load(rootElement.element("BackgroundColor")));
		setPlaybackColor(ColorXMLUtils.load(rootElement.element("PlaybackColor")));
		setWarnColor(ColorXMLUtils.load(rootElement.element("WarnColor")));
		setFadeColor(ColorXMLUtils.load(rootElement.element("FadeColor")));
		setAccentColor(ColorXMLUtils.load(rootElement.element("AccentColor")));

		Element indexLabelEmement = rootElement.element("InfoLabel");
		if (indexLabelEmement != null) {
			setInfoLabelColor(ColorXMLUtils.load(indexLabelEmement.element("Color")));
			setInfoLabelFontSize(Integer.valueOf(indexLabelEmement.element("FontSize").getStringValue()));
		}

		Element titleLabelElement = rootElement.element("TitleLabel");
		if (titleLabelElement != null) {
			setTitleLabelColor(ColorXMLUtils.load(titleLabelElement.element("Color")));
			setTitleLabelFontSize(Integer.valueOf(titleLabelElement.element("FontSize").getStringValue()));
		}
	}

	@Override
	public void save(Element element) {
		element.addElement("Theme").addText(themeProperty.get().name());
		element.addElement("CustomLayout").addText(String.valueOf(customLayout));

		ColorXMLUtils.save(element.addElement("BackgroundColor"), backgroundColor);
		ColorXMLUtils.save(element.addElement("PlaybackColor"), playbackColor);
		ColorXMLUtils.save(element.addElement("WarnColor"), warnColor);
		ColorXMLUtils.save(element.addElement("FadeColor"), fadeColor);
		ColorXMLUtils.save(element.addElement("AccentColor"), accentColor);

		Element indexLabelElement = element.addElement("InfoLabel");
		ColorXMLUtils.save(indexLabelElement.addElement("Color"), infoLabelColor);
		indexLabelElement.addElement("FontSize").addText(String.valueOf(infoLabelFontSize));

		Element titleLabelElement = element.addElement("TitleLabel");
		ColorXMLUtils.save(titleLabelElement.addElement("Color"), titleLabelColor);
		titleLabelElement.addElement("FontSize").addText(String.valueOf(titleLabelFontSize));
	}

	@Override
	public GlobalLayout clone() throws CloneNotSupportedException {
		ClassicGlobalLayout layout = (ClassicGlobalLayout) super.clone();

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
		layout.infoLabelFontSize = infoLabelFontSize;
		layout.titleLabelFontSize = titleLabelFontSize;

		return layout;
	}

	public String convertToCSS(String classSufix, boolean fullCss) {
		StringBuilder builder = new StringBuilder();

		if (classSufix.isEmpty()) {
			builder.append(".fonticon {\n");
			builder.append("\t-fx-text-fill: " + accentColor + ";\n");
			builder.append("}\n");

			builder.append(".progress-bar {\n");
			builder.append("\t-fx-accent: " + accentColor + ";\n");
			builder.append("}\n");

			builder.append(".slider .thumb {\n");
			builder.append("\t-fx-base: " + accentColor + ";\n");
			builder.append("}\n");
		}

		if (fullCss) {
			builder.append(".pad" + classSufix + " {\n");
			builder.append("\t-fx-background-color: " + backgroundColor + ";\n");
			builder.append("}\n");

			builder.append(".pad" + classSufix + ":play {\n");
			builder.append("\t-fx-background-color: " + playbackColor + ";\n");
			builder.append("}\n");

			builder.append(".pad" + classSufix + ":warn {\n");
			builder.append("\t-fx-background-color: " + warnColor + ";\n");
			builder.append("}\n");

			builder.append(".pad" + classSufix + ":fade {\n");
			builder.append("\t-fx-background-color: " + fadeColor + ";\n");
			builder.append("}\n");

			builder.append(".pad" + classSufix + "-info {\n");
			builder.append("\t-fx-text-fill: " + infoLabelColor + ";\n");
			builder.append("\t-fx-font-size: " + infoLabelFontSize + ";\n");
			builder.append("}\n");

			builder.append(".pad" + classSufix + "-title {\n");
			builder.append("\t-fx-text-fill: " + titleLabelColor + ";\n");
			builder.append("\t-fx-font-size: " + titleLabelFontSize + ";\n");
			builder.append("}\n");
		}

		return builder.toString().replace("0x", "#");
	}

	@Override
	public void applyCss(Stage stage) {
		// Clear Old
		stage.getScene().getStylesheets().clear();

		// Add Build in Default
		stage.getScene().getStylesheets().add("de/tobias/playpad/assets/style.css");
		stage.getScene().getStylesheets().add("de/tobias/playpad/assets/classic_style.css");
		stage.getScene().getStylesheets().add(themeProperty.get().getCss());

		// User Settings
		Path userCss = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "style.css");
		if (Files.exists(userCss))
			stage.getScene().getStylesheets().add(userCss.toUri().toString());
	}

	@Override
	public void applyCssMainView(IMainViewController controller, Stage stage, Project project) {
		applyCss(stage);

		// Hard Settings
		controller.setGridColor(accentColor);

		Path path = ApplicationUtils.getApplication().getPath(PathType.CONFIGURATION, "custom_style.css");

		// Globale CSS -> Immer Akzent, und wenn isCustomLayout dann alles
		String css = convertToCSS("", isCustomLayout());

		// Pad Spezelles Layout immer
		for (Pad pad : project.getPads().values()) {
			if (pad.isCustomLayout()) {
				CartLayout layoutOpt = pad.getLayout();
				css += "\n" + layoutOpt.convertToCss(String.valueOf(pad.getIndex()), true);
			}
		}

		// Speichern der generierten CSS Datei
		try {
			Files.write(path, css.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}

		stage.getScene().getStylesheets().remove(path.toUri().toString());
		stage.getScene().getStylesheets().add(path.toUri().toString());
	}

	@Override
	public void handleWarning(IPadViewController controller, Warning warning) {
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
}