package de.tobias.playpad.viewcontroller.design;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.DisplayableColor;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.design.modern.model.ModernCartDesign;
import de.tobias.playpad.design.modern.model.ModernGlobalDesign;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.view.ColorPickerView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import java.util.function.Consumer;

public class ModernCartDesignViewController extends NVC implements IColorButton {

	@FXML
	private Button backgroundColorButton;
	@FXML
	private Button playColorButton;
	@FXML
	private CheckBox warnAnimationCheckBox;

	@FXML
	private Button resetButton;

	private ModernCartDesign design;

	private PopOver colorChooser;

	public ModernCartDesignViewController(ModernCartDesign layout) {
		load("view/option/layout", "ModernLayoutCart", Localization.getBundle());

		this.design = layout;
		setLayout();
	}

	private void setLayout() {
		backgroundColorButton.setStyle(getLinearGradientCss(design.getBackgroundColor()));
		playColorButton.setStyle(getLinearGradientCss(design.getPlayColor()));
	}

	@Override
	public void init() {
		addIconToButton(backgroundColorButton);
		addIconToButton(playColorButton);
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
		final ModernGlobalDesign design = Profile.currentProfile().getProfileSettings().getDesign();
		if (design.isFlatDesign()) {
			return "-fx-background-color: " + color.paint() + ";";
		} else {
			return "-fx-background-color: " + color.linearGradient() + ";";
		}
	}

}
