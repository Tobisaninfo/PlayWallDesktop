package de.tobias.playpad.design.modern.model;

import de.tobias.playpad.design.FeedbackDesignColorSuggester;
import de.tobias.playpad.design.modern.ModernColor;
import javafx.scene.paint.Color;

public class ModernGlobalDesign implements FeedbackDesignColorSuggester {

	private ModernColor backgroundColor = ModernColor.GRAY1;
	private ModernColor playColor = ModernColor.RED3;
	private ModernColor cueInColor = ModernColor.RED2;

	private boolean isWarnAnimation = true;

	private int infoFontSize = 14;
	private int titleFontSize = 16;

	private boolean flatDesign = true;

	public ModernColor getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(ModernColor backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public ModernColor getPlayColor() {
		return playColor;
	}

	public void setPlayColor(ModernColor playColor) {
		this.playColor = playColor;
	}

	public ModernColor getCueInColor() {
		return cueInColor;
	}

	public void setCueInColor(ModernColor cueInColor) {
		this.cueInColor = cueInColor;
	}

	public boolean isWarnAnimation() {
		return isWarnAnimation;
	}

	public void setWarnAnimation(boolean isWarnAnimation) {
		this.isWarnAnimation = isWarnAnimation;
	}

	public int getInfoFontSize() {
		return infoFontSize;
	}

	public void setInfoFontSize(int infoFontSize) {
		this.infoFontSize = infoFontSize;
	}

	public int getTitleFontSize() {
		return titleFontSize;
	}

	public void setTitleFontSize(int titleFontSize) {
		this.titleFontSize = titleFontSize;
	}

	public boolean isFlatDesign() {
		return flatDesign;
	}

	public void setFlatDesign(boolean flatDesign) {
		this.flatDesign = flatDesign;
	}

	public void reset() {
		backgroundColor = ModernColor.GRAY1;
		playColor = ModernColor.RED3;
		cueInColor = ModernColor.RED2;

		isWarnAnimation = true;

		infoFontSize = 14;
		titleFontSize = 16;

		flatDesign = false;
	}

	@Override
	public Color getDesignEventColor() {
		return Color.web(playColor.getColorHi());
	}

	@Override
	public Color getDesignDefaultColor() {
		return Color.web(backgroundColor.getColorHi());
	}
}
