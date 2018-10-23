package de.tobias.playpad.viewcontroller.design;

import de.thecodelabs.utils.ui.NVC;
import de.tobias.playpad.DisplayableColor;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.design.modern.ModernGlobalDesign;
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

public class ModernGlobalDesignViewController extends NVC {

	@FXML
	private Button backgroundColorButton;
	@FXML
	private Button playColorButton;

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
		load("view/option/layout", "ModernLayoutGlobal", PlayPadMain.getUiResourceBundle());

		this.design = design;
		setLayout();
	}

	private void setLayout() {
		backgroundColorButton.setStyle(getLinearGradientCss(design.getBackgroundColor()));
		playColorButton.setStyle(getLinearGradientCss(design.getPlayColor()));

		warnAnimationCheckBox.setSelected(design.isWarnAnimation());
		flatDesignCheckbox.setSelected(design.isFlatDesign());

		infoLabelFontSizeComboBox.setValue(design.getInfoFontSize());
		titleLabelFontSizeComboBox.setValue(design.getTitleFontSize());
	}

	@Override
	public void init() {
		warnAnimationCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			design.setWarnAnimation(c);
		});
		flatDesignCheckbox.selectedProperty().addListener((a, b, c) ->
		{
			design.setFlatDesign(c);
		});

		infoLabelFontSizeComboBox.getItems().addAll(9, 10, 12, 13, 14, 16, 18, 20, 24, 28);
		infoLabelFontSizeComboBox.valueProperty().addListener((a, b, c) ->
		{
			design.setInfoFontSize(c);
		});
		infoLabelFontSizeComboBox.setConverter(new IntegerStringConverter());

		titleLabelFontSizeComboBox.getItems().addAll(9, 10, 12, 13, 14, 16, 18, 20, 24, 28);
		titleLabelFontSizeComboBox.valueProperty().addListener((a, b, c) ->
		{
			design.setTitleFontSize(c);
		});
		titleLabelFontSizeComboBox.setConverter(new IntegerStringConverter());
	}

	@FXML
	private void resetButtonHandler(ActionEvent event) {
		design.reset();
		setLayout();
	}

	@FXML
	private void backgroundColorButtonHandler(ActionEvent event) {
		colorChooser(backgroundColorButton, design.getBackgroundColor(), (color) -> design.setBackgroundColor(color));
	}

	@FXML
	private void playColorButtonHandler(ActionEvent event) {
		colorChooser(playColorButton, design.getPlayColor(), (color) -> design.setPlayColor(color));
	}

	private void colorChooser(Button anchorNode, ModernColor startColor, Consumer<ModernColor> onFinish) {
		ColorPickerView view = new ColorPickerView(startColor, ModernColor.values(), (DisplayableColor t) ->
		{
			colorChooser.hide();

			if (t instanceof ModernColor) {
				ModernColor color = (ModernColor) t;
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
		String css = "-fx-background-color: " + color.linearGradient() + ";";
		css += "-fx-border-color: rgb(20, 20, 20);-fx-border-width: 1.5px;-fx-border-radius: 3px;";
		return css;
	}

}
