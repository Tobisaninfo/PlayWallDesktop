package de.tobias.playpad.viewcontroller.design;

import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.design.modern.ModernColor;
import de.tobias.playpad.design.modern.model.ModernCartDesign;
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
	private CheckBox backgroundColorCheckbox;
	@FXML
	private CheckBox playColorCheckbox;
	@FXML
	private CheckBox cueInColorCheckbox;

	@FXML
	private Button backgroundColorButton;
	@FXML
	private Button playColorButton;
	@FXML
	private Button cueInColorButton;
	@FXML
	private CheckBox warnAnimationCheckBox;

	@FXML
	private Button resetButton;

	private final ModernCartDesign design;

	private PopOver colorChooser;

	public ModernCartDesignViewController(ModernCartDesign design) {
		load("view/option/layout", "ModernLayoutCart", Localization.getBundle());

		this.design = design;
		setDesign();
	}

	private void setDesign() {
		backgroundColorButton.setStyle(getLinearGradientCss(design.getBackgroundColor()));
		playColorButton.setStyle(getLinearGradientCss(design.getPlayColor()));
		cueInColorButton.setStyle(getLinearGradientCss(design.getCueInColor()));

		backgroundColorCheckbox.setSelected(!design.getBackgroundColor().equals(ModernCartDesign.DEFAULT_COLOR_BACKGROUND));
		playColorCheckbox.setSelected(!design.getPlayColor().equals(ModernCartDesign.DEFAULT_COLOR_PLAY));
		cueInColorCheckbox.setSelected(!design.getCueInColor().equals(ModernCartDesign.DEFAULT_COLOR_CUE_IN));
	}

	@Override
	public void init() {
		backgroundColorCheckbox.selectedProperty().addListener((a, b, c) ->
		{
			design.setEnableCustomBackgroundColor(c);
			backgroundColorButton.setDisable(!c);
		});
		playColorCheckbox.selectedProperty().addListener((a, b, c) ->
		{
			design.setEnableCustomPlayColor(c);
			playColorButton.setDisable(!c);
		});
		cueInColorCheckbox.selectedProperty().addListener((a, b, c) ->
		{
			design.setEnableCustomCueInColor(c);
			cueInColorButton.setDisable(!c);
		});

		backgroundColorButton.setDisable(true);
		playColorButton.setDisable(true);
		cueInColorButton.setDisable(true);

		addIconToButton(backgroundColorButton);
		addIconToButton(playColorButton);
		addIconToButton(cueInColorButton);
	}

	@FXML
	private void resetButtonHandler(ActionEvent event) {
		design.reset();
		setDesign();
	}

	@FXML
	private void backgroundColorButtonHandler(ActionEvent event) {
		colorChooser(backgroundColorButton, design.getBackgroundColor(), design::setBackgroundColor);
	}

	@FXML
	private void playColorButtonHandler(ActionEvent event) {
		colorChooser(playColorButton, design.getPlayColor(), design::setPlayColor);
	}

	@FXML
	private void cueInColorButtonHandler(ActionEvent event) {
		colorChooser(cueInColorButton, design.getPlayColor(), design::setCueInColor);
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
		if(Profile.currentProfile().getProfileSettings().getDesign().isFlatDesign()) {
			return "-fx-background-color: " + color.paint() + ";";
		} else {
			return "-fx-background-color: " + color.linearGradient() + ";";
		}
	}
}
