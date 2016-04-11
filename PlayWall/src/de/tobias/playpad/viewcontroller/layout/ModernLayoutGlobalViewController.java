package de.tobias.playpad.viewcontroller.layout;

import java.util.function.Consumer;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import de.tobias.playpad.DisplayableColor;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.layout.GlobalLayout;
import de.tobias.playpad.layout.modern.ModernColor;
import de.tobias.playpad.layout.modern.ModernLayoutGlobal;
import de.tobias.playpad.view.ColorView;
import de.tobias.playpad.viewcontroller.GlobalLayoutViewController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.util.converter.IntegerStringConverter;

public class ModernLayoutGlobalViewController extends GlobalLayoutViewController {

	@FXML private Button backgroundColorButton;
	@FXML private Button playColorButton;

	@FXML private CheckBox warnAnimationCheckBox;

	@FXML private ComboBox<Integer> infoLabelFontSizeComboBox;
	@FXML private ComboBox<Integer> titleLabelFontSizeComboBox;

	@FXML private Button resetButton;

	private ModernLayoutGlobal globalLayout;

	private PopOver colorChooser;

	public ModernLayoutGlobalViewController(GlobalLayout layout) {
		super("modernLayoutGlobal", "de/tobias/playpad/assets/view/option/layout/", PlayPadMain.getUiResourceBundle(), layout);

		if (layout instanceof GlobalLayout) {
			this.globalLayout = (ModernLayoutGlobal) layout;

			setLayout();
		}
	}

	private void setLayout() {
		backgroundColorButton.setStyle(getLinearGradientCss(globalLayout.getBackgroundColor()));
		playColorButton.setStyle(getLinearGradientCss(globalLayout.getPlayColor()));

		warnAnimationCheckBox.setSelected(globalLayout.isWarnAnimation());

		infoLabelFontSizeComboBox.setValue(globalLayout.getInfoFontSize());
		titleLabelFontSizeComboBox.setValue(globalLayout.getTitleFontSize());
	}

	@Override
	public void init() {
		warnAnimationCheckBox.selectedProperty().addListener((a, b, c) ->
		{
			globalLayout.setWarnAnimation(c);
		});

		infoLabelFontSizeComboBox.getItems().addAll(9, 10, 12, 13, 14, 16, 18, 20, 24, 28);
		infoLabelFontSizeComboBox.valueProperty().addListener((a, b, c) ->
		{
			globalLayout.setInfoFontSize(c);
		});
		infoLabelFontSizeComboBox.setConverter(new IntegerStringConverter());

		titleLabelFontSizeComboBox.getItems().addAll(9, 10, 12, 13, 14, 16, 18, 20, 24, 28);
		titleLabelFontSizeComboBox.valueProperty().addListener((a, b, c) ->
		{
			globalLayout.setTitleFontSize(c);
		});
		titleLabelFontSizeComboBox.setConverter(new IntegerStringConverter());
	}

	@FXML
	private void resetButtonHandler(ActionEvent event) {
		globalLayout.reset();
		setLayout();
	}

	@FXML
	private void backgroundColorButtonHandler(ActionEvent event) {
		colorChooser(backgroundColorButton, globalLayout.getBackgroundColor(), (color) -> globalLayout.setBackgroundColor(color));
	}

	@FXML
	private void playColorButtonHandler(ActionEvent event) {
		colorChooser(playColorButton, globalLayout.getPlayColor(), (color) -> globalLayout.setPlayColor(color));
	}

	private void colorChooser(Button anchorNode, ModernColor startColor, Consumer<ModernColor> onFinish) {
		ColorView view = new ColorView(startColor, ModernColor.values(), (DisplayableColor t) ->
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
