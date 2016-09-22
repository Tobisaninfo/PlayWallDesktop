package de.tobias.playpad.viewcontroller.design;

import java.util.function.Consumer;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import de.tobias.playpad.DisplayableColor;
import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.design.CartDesign;
import de.tobias.playpad.design.modern.ModernCartDesign;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.view.ColorPickerView;
import de.tobias.playpad.viewcontroller.CartDesignViewController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;

public class ModernCartDesignViewController extends CartDesignViewController {

	@FXML private Button backgroundColorButton;
	@FXML private Button playColorButton;
	@FXML private CheckBox warnAnimationCheckBox;

	@FXML private Button resetButton;

	private ModernCartDesign cartLayout;

	private PopOver colorChooser;

	public ModernCartDesignViewController(CartDesign layout) {
		super("modernLayoutCart", "de/tobias/playpad/assets/view/option/layout/", PlayPadMain.getUiResourceBundle(), layout);

		if (layout instanceof CartDesign) {
			this.cartLayout = (ModernCartDesign) layout;

			setLayout();
		}
	}

	private void setLayout() {
		backgroundColorButton.setStyle(getLinearGradientCss(cartLayout.getBackgroundColor()));
		playColorButton.setStyle(getLinearGradientCss(cartLayout.getPlayColor()));
	}

	@Override
	public void init() {
	}

	@FXML
	private void resetButtonHandler(ActionEvent event) {
		cartLayout.reset();
		setLayout();
	}

	@FXML
	private void backgroundColorButtonHandler(ActionEvent event) {
		colorChooser(backgroundColorButton, cartLayout.getBackgroundColor(), (color) -> cartLayout.setBackgroundColor(color));
	}

	@FXML
	private void playColorButtonHandler(ActionEvent event) {
		colorChooser(playColorButton, cartLayout.getPlayColor(), (color) -> cartLayout.setPlayColor(color));
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
