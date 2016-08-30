package de.tobias.playpad.viewcontroller.option.feedback;

import java.util.Optional;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.action.feedback.DisplayableFeedbackColor;
import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.action.mapper.feedback.SingleMidiFeedback;
import de.tobias.playpad.action.mididevice.Device;
import de.tobias.playpad.midi.Midi;
import de.tobias.playpad.view.ColorView;
import de.tobias.utils.ui.ContentViewController;
import de.tobias.utils.ui.icon.FontAwesomeType;
import de.tobias.utils.ui.icon.FontIcon;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class SingleFeedbackViewController extends ContentViewController {

	@FXML private HBox defaultColorParent;
	@FXML private Button colorChooseDefaultButton;
	@FXML private Rectangle colorPreviewDefault;

	private PopOver colorChooser;

	private SingleMidiFeedback feedback;

	public SingleFeedbackViewController(SingleMidiFeedback feedback, DisplayableFeedbackColor[] colors) {
		super("singleFeedback", "de/tobias/playpad/assets/view/option/feedback/", PlayPadMain.getUiResourceBundle());
		this.feedback = feedback;

		Optional<Device> deviceOptional = Midi.getInstance().getMidiDevice();
		deviceOptional.ifPresent(device ->
		{
			DisplayableFeedbackColor colorDefault = device.getColor(feedback.getValueForFeedbackMessage(FeedbackMessage.STANDARD));
			if (colorDefault != null) {
				colorPreviewDefault.setFill(colorDefault.getPaint());
				setColorChooseButtonColor(colorDefault.getPaint(), colorChooseDefaultButton);
			}
		});
	}

	@Override
	public void init() {
		FontIcon iconDefault = new FontIcon(FontAwesomeType.ARROW_CIRCLE_DOWN);
		iconDefault.getStyleClass().remove(FontIcon.STYLE_CLASS);
		colorChooseDefaultButton.setGraphic(iconDefault);
		colorPreviewDefault.widthProperty().bind(colorChooseDefaultButton.widthProperty());
	}

	@FXML
	private void colorChooseButtonHandler(ActionEvent event) {
		if (colorChooser == null) {
			colorChooser = new PopOver();
			Midi.getInstance().getMidiDevice().ifPresent((device) ->
			{
				DisplayableFeedbackColor color = device.getColor(feedback.getValueForFeedbackMessage(FeedbackMessage.STANDARD));

				ColorView colorView = new ColorView(color, device.getColors(), item ->
				{
					colorChooser.hide();
					if (item instanceof DisplayableFeedbackColor) {
						if (event.getSource() == colorChooseDefaultButton) {
							feedback.setFeedbackValue(((DisplayableFeedbackColor) item).mapperFeedbackValue());
							colorPreviewDefault.setFill(item.getPaint());
							setColorChooseButtonColor(item.getPaint(), colorChooseDefaultButton);
						}
					}
				});

				colorChooser.setContentNode(colorView);
				colorChooser.setDetachable(false);
				colorChooser.setOnHiding(e -> colorChooser = null);
				colorChooser.setCornerRadius(5);
				colorChooser.setArrowLocation(ArrowLocation.LEFT_CENTER);
				colorChooser.show((Node) event.getSource());
			});
		}
	}

	private void setColorChooseButtonColor(Paint inputColor, Button button) {
		Color color;
		if (inputColor.equals(Color.BLACK))
			color = Color.WHITE;
		else
			color = Color.BLACK;
		((FontIcon) button.getGraphic()).setColor(color);
	}
}
