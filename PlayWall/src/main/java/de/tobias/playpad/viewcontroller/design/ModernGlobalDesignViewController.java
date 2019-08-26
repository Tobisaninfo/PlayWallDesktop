package de.tobias.playpad.viewcontroller.design;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.design.modern.model.ModernGlobalDesign;
import de.tobias.playpad.view.ColorPickerView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.util.converter.IntegerStringConverter;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import java.util.function.Consumer;

public class ModernGlobalDesignViewController extends NVC implements IColorButton {

	@FXML
	private Button backgroundColorButton;
	@FXML
	private Button playColorButton;
	@FXML
	private Button cueInColorButton;

	@FXML
	private CheckBox warnAnimationCheckBox;

	@FXML
	private ComboBox<Integer> infoLabelFontSizeComboBox;
	@FXML
	private ComboBox<Integer> titleLabelFontSizeComboBox;

	@FXML
	private Button resetButton;

	@FXML
	private CheckBox flatDesignCheckbox;

	private ModernGlobalDesign design;

	private PopOver colorChooser;

	public ModernGlobalDesignViewController(ModernGlobalDesign design) {
		load("view/option/layout", "ModernLayoutGlobal", Localization.getBundle());

		this.design = design;
		setLayout();
	}

	private void setLayout() {
		backgroundColorButton.setStyle(getLinearGradientCss(design.getBackgroundColor()));
		playColorButton.setStyle(getLinearGradientCss(design.getPlayColor()));
		cueInColorButton.setStyle(getLinearGradientCss(design.getCueInColor()));

		warnAnimationCheckBox.setSelected(design.isWarnAnimation());
		flatDesignCheckbox.setSelected(design.isFlatDesign());

		infoLabelFontSizeComboBox.setValue(design.getInfoFontSize());
		titleLabelFontSizeComboBox.setValue(design.getTitleFontSize());
	}

	@Override
	public void init() {
		warnAnimationCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> design.setWarnAnimation(newValue));

		flatDesignCheckbox.selectedProperty().addListener((observable, oldValue, newValue) ->
		{
			design.setFlatDesign(newValue);

			// Update button preview
			backgroundColorButton.setStyle(getLinearGradientCss(design.getBackgroundColor()));
			playColorButton.setStyle(getLinearGradientCss(design.getPlayColor()));
		});

		infoLabelFontSizeComboBox.getItems().addAll(9, 10, 12, 13, 14, 16, 18, 20, 24, 28);
		infoLabelFontSizeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> design.setInfoFontSize(newValue));
		infoLabelFontSizeComboBox.setConverter(new IntegerStringConverter());

		titleLabelFontSizeComboBox.getItems().addAll(9, 10, 12, 13, 14, 16, 18, 20, 24, 28);
		titleLabelFontSizeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> design.setTitleFontSize(newValue));
		titleLabelFontSizeComboBox.setConverter(new IntegerStringConverter());

		addIconToButton(backgroundColorButton);
		addIconToButton(playColorButton);
		addIconToButton(cueInColorButton);
	}

	@FXML
	private void resetButtonHandler(ActionEvent event) {
		design.reset();
		setLayout();
	}

	@FXML
	private void backgroundColorButtonHandler(ActionEvent event) {
		colorChooser(backgroundColorButton, design.getBackgroundColor(), color -> design.setBackgroundColor(color));
	}

	@FXML
	private void playColorButtonHandler(ActionEvent event) {
		colorChooser(playColorButton, design.getPlayColor(), color -> design.setPlayColor(color));
	}

	@FXML
	private void cueInColorButtonHandler(ActionEvent event) {
		colorChooser(cueInColorButton, design.getPlayColor(), color -> design.setCueInColor(color));
	}

	private void colorChooser(Button anchorNode, ModernColor startColor, Consumer<ModernColor> onFinish) {
		ColorPickerView view = new ColorPickerView(startColor, ModernColor.values(), newValue ->
		{
			colorChooser.hide();

			if (newValue instanceof ModernColor) {
				ModernColor color = (ModernColor) newValue;
				onFinish.accept(color);
				anchorNode.setStyle(getLinearGradientCss(color));
			}
		});
		colorChooser = new PopOver();
		colorChooser.setContentNode(view);
		colorChooser.setDetachable(false);
		colorChooser.setOnHiding(e -> colorChooser = null);
		colorChooser.setCornerRadius(5);
		colorChooser.setArrowLocation(ArrowLocation.LEFT_CENTER);
		colorChooser.show(anchorNode);
	}

	private String getLinearGradientCss(ModernColor color) {
		if (design.isFlatDesign()) {
			return "-fx-background-color: " + color.paint() + ";";
		} else {
			return "-fx-background-color: " + color.linearGradient() + ";";
		}
	}
}
