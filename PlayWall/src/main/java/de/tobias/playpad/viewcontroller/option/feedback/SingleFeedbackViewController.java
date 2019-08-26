package de.tobias.playpad.viewcontroller.option.feedback;

import de.thecodelabs.midi.action.Action;
import de.thecodelabs.midi.feedback.Feedback;
import de.thecodelabs.midi.feedback.FeedbackColor;
import de.thecodelabs.midi.feedback.FeedbackType;
import de.thecodelabs.midi.feedback.FeedbackValue;
import de.thecodelabs.midi.midi.Midi;
import de.thecodelabs.utils.ui.NVC;
import de.thecodelabs.utils.ui.icon.FontIcon;
import de.thecodelabs.utils.util.ColorUtils;
import de.thecodelabs.utils.util.Localization;
import de.tobias.playpad.action.feedback.LightMode;
import de.tobias.playpad.profile.Profile;
import de.tobias.playpad.profile.ProfileSettings;
import de.tobias.playpad.view.FeedbackColorPickerView;
import de.tobias.playpad.viewcontroller.design.IColorButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import java.util.Arrays;
import java.util.List;

public class SingleFeedbackViewController extends NVC implements IColorButton {

	@FXML
	private Label nameLabel;
	@FXML
	private HBox defaultColorParent;
	@FXML
	private Button colorChooseDefaultButton;
	@FXML
	private Rectangle colorPreviewDefault;

	private PopOver colorChooser;

	private FeedbackColor[] colors;
	private Feedback feedback;
	private Action action;

	public SingleFeedbackViewController(Feedback feedback, FeedbackType type, FeedbackValue[] values, Action action) {
		load("view/option/feedback", "SingleFeedback", Localization.getBundle());
		this.feedback = feedback;
		this.action = action;

		if (!(values instanceof FeedbackColor[])) {
			throw new IllegalArgumentException("FeedbackValues are not of type FeedbackColor");
		}

		switch (type) {
			case DEFAULT:
				nameLabel.setText(Localization.getString("feedback.label.colorDefault"));
				break;
			case EVENT:
				nameLabel.setText(Localization.getString("feedback.label.colorEvent"));
				break;
			case WARNING:
				nameLabel.setText(Localization.getString("feedback.label.colorWarning"));
				break;
		}

		colors = (FeedbackColor[]) values;

		final FeedbackColor feedbackColor = getFeedbackColor(feedback);
		if (feedbackColor != null) {
			setColorChooseButtonColor(feedbackColor.getColor(), colorChooseDefaultButton);
		}
	}

	private FeedbackColor getFeedbackColor(Feedback feedback) {
		for (FeedbackColor color : colors) {
			if (color.getValue() == feedback.getValue()) {
				return color;
			}
		}
		return null;
	}

	@Override
	public void init() {
		addIconToButton(colorChooseDefaultButton);
		colorPreviewDefault.widthProperty().bind(colorChooseDefaultButton.widthProperty());
	}

	@FXML
	private void colorChooseButtonHandler(ActionEvent event) {
		if (colorChooser == null) {
			colorChooser = new PopOver();
			final FeedbackColor feedbackColor = getFeedbackColor(feedback);

			List<? extends FeedbackColor> selectableColors = Arrays.asList(colors);
			if (colors instanceof LightMode.ILightMode[]) {
				ProfileSettings profileSettings = Profile.currentProfile().getProfileSettings();
				selectableColors = LightMode.filter((LightMode.ILightMode[]) colors, profileSettings.getLightMode());
			}

			FeedbackColorPickerView colorView = new FeedbackColorPickerView(feedbackColor, selectableColors, item ->
			{
				colorChooser.hide();
				if (event.getSource() == colorChooseDefaultButton) {
					feedback.setValue(item.getValue());
					setColorChooseButtonColor(item.getColor(), colorChooseDefaultButton);

					// highlight current button on device
					Midi.getInstance().clearFeedback();
					Midi.getInstance().showFeedback(action);
				}
			});

			colorChooser.setContentNode(colorView);
			colorChooser.setDetachable(false);
			colorChooser.setOnHiding(e -> colorChooser = null);
			colorChooser.setCornerRadius(5);
			colorChooser.setArrowLocation(ArrowLocation.LEFT_CENTER);
			colorChooser.show((Node) event.getSource());
		}
	}


	private void setColorChooseButtonColor(Paint inputColor, Button button) {
		colorPreviewDefault.setFill(inputColor);
		if (inputColor instanceof Color) {
			((FontIcon) button.getGraphic()).setColor(ColorUtils.getAppropriateTextColor((Color) inputColor));
		} else {
			((FontIcon) button.getGraphic()).setColor(Color.BLACK);
		}
	}
}
