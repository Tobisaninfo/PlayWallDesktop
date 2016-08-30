package de.tobias.playpad.viewcontroller.option.feedback;

import java.util.Optional;

import org.controlsfx.control.PopOver;
import org.controlsfx.control.PopOver.ArrowLocation;

import de.tobias.playpad.PlayPadMain;
import de.tobias.playpad.action.feedback.DisplayableFeedbackColor;
import de.tobias.playpad.action.feedback.FeedbackMessage;
import de.tobias.playpad.action.mapper.feedback.DoubleMidiFeedback;
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

public class DoubleFeedbackViewController extends ContentViewController {

	@FXML private HBox defaultColorParent;
	@FXML private Button colorChooseDefaultButton;
	@FXML private Rectangle colorPreviewDefault;

	@FXML private HBox eventColorParent;
	@FXML private Button colorChooseEventButton;
	@FXML private Rectangle colorPreviewEvent;

	private PopOver colorChooser;

	private DoubleMidiFeedback feedback;

	public DoubleFeedbackViewController(DoubleMidiFeedback feedback, DisplayableFeedbackColor[] colors) {
		super("doubleFeedback", "de/tobias/playpad/assets/view/option/feedback/", PlayPadMain.getUiResourceBundle());
		this.feedback = feedback;

		Optional<Device> deviceOptional = Midi.getInstance().getMidiDevice();
		deviceOptional.ifPresent(device ->
		{
			DisplayableFeedbackColor colorDefault = device.getColor(feedback.getValueForFeedbackMessage(FeedbackMessage.STANDARD));
			if (colorDefault != null) {
				colorPreviewDefault.setFill(colorDefault.getPaint());
				setColorChooseButtonColor(colorDefault.getPaint(), colorChooseDefaultButton);
			}
			DisplayableFeedbackColor colorPlay = device.getColor(feedback.getValueForFeedbackMessage(FeedbackMessage.EVENT));
			if (colorPlay != null) {
				colorPreviewEvent.setFill(colorPlay.getPaint());
				setColorChooseButtonColor(colorPlay.getPaint(), colorChooseEventButton);
			}
		});
	}

	@Override
	public void init() {
		FontIcon iconDefault = new FontIcon(FontAwesomeType.ARROW_CIRCLE_DOWN);
		iconDefault.getStyleClass().remove(FontIcon.STYLE_CLASS);
		colorChooseDefaultButton.setGraphic(iconDefault);
		colorPreviewDefault.widthProperty().bind(colorChooseDefaultButton.widthProperty());

		FontIcon iconEvent = new FontIcon(FontAwesomeType.ARROW_CIRCLE_DOWN);
		iconEvent.getStyleClass().remove(FontIcon.STYLE_CLASS);
		colorChooseEventButton.setGraphic(iconEvent);
		colorPreviewEvent.widthProperty().bind(colorChooseEventButton.widthProperty());
	}

	@FXML
	private void colorChooseButtonHandler(ActionEvent event) {
		if (colorChooser == null) {
			colorChooser = new PopOver();
			Midi.getInstance().getMidiDevice().ifPresent((device) ->
			{
				DisplayableFeedbackColor color = device.getColor(feedback.getValueForFeedbackMessage(FeedbackMessage.STANDARD));
				if (event.getSource() == colorChooseDefaultButton) {
					color = device.getColor(feedback.getValueForFeedbackMessage(FeedbackMessage.STANDARD));
				} else if (event.getSource() == colorChooseEventButton) {
					color = device.getColor(feedback.getValueForFeedbackMessage(FeedbackMessage.EVENT));
				}
				
				ColorView colorView = new ColorView(color, device.getColors(), item ->
				{
					colorChooser.hide();
					if (item instanceof DisplayableFeedbackColor) {
						if (event.getSource() == colorChooseDefaultButton) {
							feedback.setFeedbackDefaultValue(((DisplayableFeedbackColor) item).mapperFeedbackValue());
							colorPreviewDefault.setFill(item.getPaint());
							setColorChooseButtonColor(item.getPaint(), colorChooseDefaultButton);
						} else if (event.getSource() == colorChooseEventButton) {
							feedback.setFeedbackEventValue(((DisplayableFeedbackColor) item).mapperFeedbackValue());
							colorPreviewEvent.setFill(item.getPaint());
							setColorChooseButtonColor(item.getPaint(), colorChooseEventButton);
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
